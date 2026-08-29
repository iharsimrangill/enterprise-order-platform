package com.portfolio.inventory.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
public class KafkaConsumerRecoveryConfiguration {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerRecoveryConfiguration.class);

    @Bean
    CommonErrorHandler kafkaCommonErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${app.kafka.recovery.backoff-ms:1000}") long backoffMs,
            @Value("${app.kafka.recovery.max-retries:2}") long maxRetries,
            @Value("${app.kafka.recovery.dlt-suffix:.dlt}") String dltSuffix) {

        var recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> deadLetterDestination(record, dltSuffix));

        var errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(backoffMs, maxRetries));

        errorHandler.setRetryListeners((record, exception, deliveryAttempt) ->
                log.atWarn()
                        .addKeyValue("topic", record.topic())
                        .addKeyValue("partition", record.partition())
                        .addKeyValue("offset", record.offset())
                        .addKeyValue("deliveryAttempt", deliveryAttempt)
                        .addKeyValue("exception", exception == null ? "unknown" : exception.getClass().getSimpleName())
                        .log("Kafka record delivery failed"));

        return errorHandler;
    }

    static TopicPartition deadLetterDestination(ConsumerRecord<?, ?> record, String dltSuffix) {
        return new TopicPartition(record.topic() + dltSuffix, record.partition());
    }
}
