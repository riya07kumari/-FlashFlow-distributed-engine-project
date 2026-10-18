package com.flashflow.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "flashflow.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaTopicConfig {

    public static final String TOPIC_ORDERS = "flashflow.orders";
    public static final String TOPIC_PAYMENTS = "flashflow.payments";
    public static final String TOPIC_INVENTORY_ROLLBACK = "flashflow.inventory.rollback";

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(TOPIC_ORDERS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentsTopic() {
        return TopicBuilder.name(TOPIC_PAYMENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic inventoryRollbackTopic() {
        return TopicBuilder.name(TOPIC_INVENTORY_ROLLBACK)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
