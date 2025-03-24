package com.user.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

	private static final String BOOTSTRAP_SERVERS = "localhost:9092";

	@Bean
	public KafkaAdmin kafkaAdmin() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		return new KafkaAdmin(configProps);
	}

	// ✅ Create user-registration Topic
	@Bean
	public NewTopic userRegistrationTopic() {
		return TopicBuilder.name("user-registration").partitions(3).replicas(2).build();
	}

	// ✅ Create transaction-events Topic
	@Bean
	public NewTopic transactionEventsTopic() {
		return TopicBuilder.name("transaction-events").partitions(3).replicas(2).build();
	}
}
