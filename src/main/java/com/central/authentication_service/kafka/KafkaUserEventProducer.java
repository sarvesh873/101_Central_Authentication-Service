package com.central.authentication_service.kafka;

import com.central.authentication.UserEvent;
import com.central.authentication_service.model.User;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class KafkaUserEventProducer {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final String USER_EVENT_TOPIC;

    @Autowired
    public KafkaUserEventProducer(
            KafkaTemplate<String, byte[]> kafkaTemplate,
            @Value("${kafka.topics.user-events}") String USER_EVENT_TOPIC) {
        this.kafkaTemplate = kafkaTemplate;
        this.USER_EVENT_TOPIC = USER_EVENT_TOPIC;
        log.info("Initialized Kafka producer for topic: {}", USER_EVENT_TOPIC);
    }

    /**
     * Sends a user event to Kafka
     * @param user The user details from the User model
     */
    public void sendUserEvent(User user) {
        if (user == null) {
            log.error("Cannot send null user event");
            return;
        }

        log.info("Sending user event for user: {}", user.getUserCode());

        try {
            // Build the user event
            UserEvent userEvent = UserEvent.newBuilder()
                    .setUserCode(user.getUserCode())
                    .setUsername(user.getUsername())
                    .setEmail(user.getEmail())
                    .setPhoneNumber(user.getPhoneNumber())
                    .build();

            // Send the event to Kafka
            CompletableFuture<SendResult<String, byte[]>> future =
                    kafkaTemplate.send(USER_EVENT_TOPIC, user.getUserCode(), userEvent.toByteArray());

            future.thenAccept(result -> {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Reward event sent successfully! User: {}, Topic: {}, Partition: {}",
                        user.getUserCode(), metadata.topic(), metadata.partition());
            }).exceptionally(ex -> {
                log.error("Failed to send reward event for user {}: {}",
                        user.getUserCode(), ex.getMessage(), ex);
                return null;
            });

        } catch (Exception e) {
            log.error("Error building or sending reward event: {}", e.getMessage(), e);
        }
    }
}
