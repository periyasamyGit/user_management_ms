package com.user.service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.user.dto.ApiResponse;
import com.user.dto.TransactionEvent;
import com.user.dto.UserDTO;
import com.user.dto.UserEvent;
import com.user.entity.FailedTransaction;
import com.user.entity.Users;
import com.user.repo.FailedTransactionRepository;
import com.user.repo.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {

	private static final String USER_TOPIC = "user-registration";
	private static final String TRANSACTION_TOPIC = "transaction-events";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FailedTransactionRepository failedTransactionRepository;

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	public ApiResponse<String> registerUser(UserDTO userDTO) {
		log.info("Starting user registration: username={}", userDTO.getUsername());

		if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
			log.warn("User already exists: username={}", userDTO.getUsername());
			return ApiResponse.failure("User already registered!");
		}

		Users user = new Users();
		user.setUsername(userDTO.getUsername());
		user.setEmail(userDTO.getEmail());
		user.setPhone(userDTO.getPhone());
		user.setTransactionLimit(5000.0);
		userRepository.save(user);
		log.info("User registered successfully: username={}", userDTO.getUsername());

		publishUserEvent(userDTO, "REGISTERED");
		return ApiResponse.success("User registered successfully!", null);
	}

	public ApiResponse<String> updateUser(UserDTO userDTO) {
		log.info("Updating user: username={}", userDTO.getUsername());
		Optional<Users> userOpt = userRepository.findByUsername(userDTO.getUsername());

		if (userOpt.isPresent()) {
			Users user = userOpt.get();
			user.setEmail(userDTO.getEmail());
			user.setPhone(userDTO.getPhone());
			userRepository.save(user);
			log.info("User updated successfully: username={}", userDTO.getUsername());

			publishUserEvent(userDTO, "UPDATED");
			return ApiResponse.success("User updated successfully!", null);
		}

		log.warn("User not found: username={}", userDTO.getUsername());
		return ApiResponse.failure("User not found!");
	}

	private void publishUserEvent(UserDTO userDTO, String eventType) {
		log.info("Publishing event: eventType={}, username={}", eventType, userDTO.getUsername());
		UserEvent event = new UserEvent(eventType, userDTO);
		Message<UserEvent> message = MessageBuilder.withPayload(event)
				.setHeader(KafkaHeaders.KEY, userDTO.getUsername()).setHeader("eventType", eventType).build();

		kafkaTemplate.send(USER_TOPIC, message);
		log.info("Event published successfully: eventType={}, username={}", eventType, userDTO.getUsername());
	}

	@KafkaListener(topics = TRANSACTION_TOPIC, groupId = "user-group", containerFactory = "kafkaListenerContainerFactory", concurrency = "3")
	public void processTransactionEvent(ConsumerRecord<String, TransactionEvent> record, Acknowledgment ack) {
		String consumerName = Thread.currentThread().getName();
		TransactionEvent event = record.value();
		log.info("Received transaction event: consumer={}, transactionId={}, username={}, partition={}, offset={}",
				consumerName, event.getTransactionId(), event.getUsername(), record.partition(), record.offset());

		boolean isReprocessing = record.headers().lastHeader("isReprocessing") != null && Boolean.parseBoolean(
				new String(record.headers().lastHeader("isReprocessing").value(), StandardCharsets.UTF_8));

		try {
			processTransaction(event);
			ack.acknowledge();
			log.info("Transaction processed successfully: consumer={}, transactionId={}", consumerName,
					event.getTransactionId());
		} catch (Exception e) {
			log.error("Transaction processing failed: consumer={}, transactionId={}, error={}", consumerName,
					event.getTransactionId(), e.getMessage());
			if (isReprocessing) {
				moveToPermanentFailureTopic(event, e.getMessage(), true);
			} else {
				moveToDLQAndSaveToDB(event, e.getMessage(), false);
			}
			ack.acknowledge();
		}
	}

	private void processTransaction(TransactionEvent event) {

		log.info("Processing transaction: transactionId={}, username={}", event.getTransactionId(),
				event.getUsername());

		Optional<Users> userOpt = userRepository.findByUsername(event.getUsername());

		if (userOpt.isPresent()) {
			Users user = userOpt.get();
			log.info("User found for transaction: {}", event.getTransactionId());

			if ("CREDIT".equalsIgnoreCase(event.getTransactionType())) {
				double newLimit = user.getTransactionLimit() + event.getAmount();
				user.setTransactionLimit(newLimit);
				log.info("Credited ₹{} to user '{}'. New transaction limit: ₹{}", event.getAmount(), user.getUsername(),
						newLimit);
			} else if ("DEBIT".equalsIgnoreCase(event.getTransactionType())) {
				double newLimit = user.getTransactionLimit() - event.getAmount();
				if (newLimit < 0) {
					log.warn("Transaction exceeds limit for user '{}'. Transaction ID: {}", user.getUsername(),
							event.getTransactionId());
					return;
				}
				user.setTransactionLimit(newLimit);
				log.info("Debited ₹{} from user '{}'. New transaction limit: ₹{}", event.getAmount(),
						user.getUsername(), newLimit);
			}

			userRepository.save(user);
			log.info("Updated transaction limit successfully for user: {}", user.getUsername());

		} else {
			log.error("User not found for transaction ID: {}", event.getTransactionId());
		}
	}

	private void moveToDLQAndSaveToDB(TransactionEvent event, String errorMessage, boolean isReprocessed) {
		log.warn("Moving transaction to DLQ: transactionId={}, error={}", event.getTransactionId(), errorMessage);
		saveFailedEventToDB(event, errorMessage, isReprocessed);
		kafkaTemplate.send("transaction-dlq", MessageBuilder.withPayload(event)
				.setHeader(KafkaHeaders.KEY, event.getTransactionId()).setHeader("isReprocessing", "false").build());
	}

	private void moveToPermanentFailureTopic(TransactionEvent event, String errorMessage, boolean isReprocessed) {
		log.warn("Moving transaction to permanent failure topic: transactionId={}, error={}", event.getTransactionId(),
				errorMessage);
		saveFailedEventToDB(event, errorMessage, isReprocessed);
		kafkaTemplate.send("transaction-permanent-fail-topic", MessageBuilder.withPayload(event)
				.setHeader(KafkaHeaders.KEY, event.getTransactionId()).setHeader("isReprocessing", "true").build());
	}

	private void saveFailedEventToDB(TransactionEvent event, String errorMessage, boolean isReprocessed) {
		FailedTransaction failedTransaction = new FailedTransaction();
		failedTransaction.setTransactionId(event.getTransactionId());
		failedTransaction.setUsername(event.getUsername());
		failedTransaction.setTransactionType(event.getTransactionType());
		failedTransaction.setAmount(event.getAmount());
		failedTransaction.setErrorMessage(errorMessage);
		failedTransaction.setStatus("FAILED"); // Save status as failed
		failedTransaction.setReprocessed(isReprocessed); // Set the reprocessed flag
		failedTransactionRepository.save(failedTransaction); // Save to DB
	}
}
