package com.portfolio.orders.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaConsumerRecoveryConfigurationTest {

    @Test
    void preservesPartitionAndAppendsDltSuffix() {
        var record = new ConsumerRecord<String, String>("inventory.reserved.v1", 3, 42L, "order-1", "payload");

        var destination = KafkaConsumerRecoveryConfiguration.deadLetterDestination(record, ".dlt");

        assertThat(destination.topic()).isEqualTo("inventory.reserved.v1.dlt");
        assertThat(destination.partition()).isEqualTo(3);
    }
}
