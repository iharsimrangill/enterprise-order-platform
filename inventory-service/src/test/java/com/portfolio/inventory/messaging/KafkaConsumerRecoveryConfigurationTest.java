package com.portfolio.inventory.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaConsumerRecoveryConfigurationTest {

    @Test
    void preservesPartitionAndAppendsDltSuffix() {
        var record = new ConsumerRecord<String, String>("orders.created.v1", 2, 17L, "order-1", "payload");

        var destination = KafkaConsumerRecoveryConfiguration.deadLetterDestination(record, ".dlt");

        assertThat(destination.topic()).isEqualTo("orders.created.v1.dlt");
        assertThat(destination.partition()).isEqualTo(2);
    }
}
