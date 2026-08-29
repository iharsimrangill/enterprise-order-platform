package com.portfolio.inventory.messaging;

import com.portfolio.inventory.messaging.event.OrderCreatedEvent;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.SerializationUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MalformedKafkaPayloadHandlingTest {

    @Test
    void capturesMalformedOrderCreatedBeforeListenerInvocation() {
        var delegate = new JacksonJsonDeserializer<>(OrderCreatedEvent.class)
                .ignoreTypeHeaders()
                .trustedPackages("com.portfolio.inventory.messaging.event");
        var deserializer = new ErrorHandlingDeserializer<>(delegate);
        var headers = new RecordHeaders();
        var rawPayload = "{not-valid-json".getBytes(StandardCharsets.UTF_8);

        try {
            var result = deserializer.deserialize("orders.created.v1", headers, rawPayload);

            assertThat(result).isNull();
            assertThat(headers.lastHeader(SerializationUtils.VALUE_DESERIALIZER_EXCEPTION_HEADER)).isNotNull();
        } finally {
            deserializer.close();
        }
    }

    @Test
    void deadLetterSerializerPreservesMalformedRawBytes() {
        var serializer = KafkaProducerSerializationConfiguration.deadLetterSafeValueSerializer();
        var rawPayload = "{not-valid-json".getBytes(StandardCharsets.UTF_8);

        try {
            assertThat(serializer.serialize("orders.created.v1.dlt", rawPayload))
                    .containsExactly(rawPayload);
        } finally {
            serializer.close();
        }
    }
}
