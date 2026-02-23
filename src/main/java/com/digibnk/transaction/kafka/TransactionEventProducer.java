package com.digibnk.transaction.kafka;

import com.digibnk.transaction.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    @Value("${app.kafka.topics.transaction-created}")
    private String topicName;

    public void publishTransactionCreated(TransactionCreatedEvent event) {
        log.info("Publishing TransactionCreatedEvent for reference: {}", event.getReference());

        // Use transaction reference as the Kafka message key → same account always goes
        // to the same partition, preserving order per account
        CompletableFuture<SendResult<String, TransactionCreatedEvent>> future =
                kafkaTemplate.send(topicName, event.getReference(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish TransactionCreatedEvent for reference: {}. Error: {}",
                        event.getReference(), ex.getMessage());
            } else {
                log.info("Published TransactionCreatedEvent for reference: {} → topic: {}, partition: {}, offset: {}",
                        event.getReference(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
