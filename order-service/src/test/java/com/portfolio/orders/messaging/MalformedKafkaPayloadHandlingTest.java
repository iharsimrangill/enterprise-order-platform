package com.portfolio.orders.messaging;

import com.portfolio.orders.messaging.event.InventoryOutcomeEvent;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.SerializationUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MalformedKafkaPayloadHandlingTest {

    @Test
    void capturesMalformedInventoryOutcomeBeforeListenerInvocation() {
        var delegate = new JacksonJsonDeserializer<>(InventoryOutcomeEvent.class)
                .ignoreTypeHeaders()
                .trustedPackages("com.portfolio.orders.messaging.event");
        var deserializer = new ErrorHandlingDeserializer<>(delegate);
        var headers = new RecordHeaders();
        var rawPayload = "{not-valid-json".getBytes(StandardCharsets.UTF_8);

        try {
            var result = deserializer.deserialize("inventory.reserved.v1", headers, rawPayload);

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
            assertThat(serializer.serialize("inventory.reserved.v1.dlt", rawPayload))
                    .containsExactly(rawPayload);
        } finally {
            serializer.close();
        }
    }
}
