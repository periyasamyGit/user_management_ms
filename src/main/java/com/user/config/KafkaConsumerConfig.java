package com.user.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class KafkaConsumerConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	// ✅ Step 1: Consumer Factory with Type Mapping
	@Bean
	public ConsumerFactory<String, Object> transactionConsumerFactory() {
		Map<String, Object> configs = new HashMap<>();
		configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		configs.put(ConsumerConfig.GROUP_ID_CONFIG, "user-group");
		configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		// Create JsonDeserializer with ObjectMapper and trusted packages
		JsonDeserializer<Object> jsonDeserializer = new JsonDeserializer<>(new ObjectMapper());
		jsonDeserializer.addTrustedPackages("com.transaction.dto", "com.user.dto");

		// ✅ Map TransactionEvent from transaction-ms to user-ms DTO
		Map<String, Class<?>> typeMappings = new HashMap<>();
		typeMappings.put("com.transaction.dto.TransactionEvent", com.user.dto.TransactionEvent.class);

		// Configure Jackson Type Mapper for type mapping
		DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
		typeMapper.setIdClassMapping(typeMappings);
		typeMapper.setTypePrecedence(DefaultJackson2JavaTypeMapper.TypePrecedence.TYPE_ID);

		// Assign type mapper to JsonDeserializer
		jsonDeserializer.setTypeMapper(typeMapper);

		// Use ErrorHandlingDeserializer to wrap JsonDeserializer
		ErrorHandlingDeserializer<Object> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

		// Create Consumer Factory
		return new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), errorHandlingDeserializer);
	}

	// ✅ Step 2: Kafka Listener Container Factory with Ack Mode and Error Handling

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {

		ExponentialBackOff backOff = new ExponentialBackOff();
		backOff.setInitialInterval(1000L); // Initial delay: 1 second
		backOff.setMultiplier(2.0); // Double the delay after each retry
		backOff.setMaxInterval(5000L); // Maximum delay: 5 seconds
		ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(transactionConsumerFactory());

		// ✅ Set Ack Mode for batch processing
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

		// ✅ Handle errors using DefaultErrorHandler
		factory.setCommonErrorHandler(new DefaultErrorHandler((record, exception) -> {
			System.out.println("⚠️ Skipping bad record: " + record + ", due to: " + exception.getMessage());
		}, backOff));

		return factory;
	}

}
