package com.portfolio.inventory.messaging;

import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.LinkedHashMap;

/**
 * Configures one Kafka template that can publish both normal JSON integration events
 * and the original raw bytes produced by ErrorHandlingDeserializer failures.
 */
@Configuration(proxyBeanMethods = false)
public class KafkaProducerSerializationConfiguration {

    @Bean
    ProducerFactory<String, Object> kafkaProducerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                deadLetterSafeValueSerializer());
    }

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> kafkaProducerFactory) {
        return new KafkaTemplate<>(kafkaProducerFactory);
    }

    static DelegatingByTypeSerializer deadLetterSafeValueSerializer() {
        var delegates = new LinkedHashMap<Class<?>, org.apache.kafka.common.serialization.Serializer<?>>();
        delegates.put(byte[].class, new ByteArraySerializer());
        delegates.put(Object.class, new JacksonJsonSerializer<>().noTypeInfo());
        return new DelegatingByTypeSerializer(delegates, true);
    }
}
