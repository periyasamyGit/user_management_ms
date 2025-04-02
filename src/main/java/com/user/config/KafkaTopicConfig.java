package com.user.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaTopicConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Bean
	public KafkaAdmin kafkaAdmin() {
		Map<String, Object> configProps = new HashMap<>();
		configProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		return new KafkaAdmin(configProps);
	}

	// ✅ Create user-registration Topic
	@Bean
	public NewTopic userRegistrationTopic() {
		return TopicBuilder.name("user-registration").partitions(3).replicas(1).build();
	}

	// ✅ Create transaction-events Topic
	@Bean
	public NewTopic transactionEventsTopic() {
		return TopicBuilder.name("transaction-events").partitions(3).replicas(1).build();
	}

	@Bean
	public NewTopic dlqTopic() {
		return TopicBuilder.name("transaction-dlq").partitions(3).replicas(1).build();
	}

	@Bean
	public NewTopic tpfTopic() {
		return TopicBuilder.name("transaction-permanent-fail-topic").partitions(3).replicas(1).build();
	}

}
