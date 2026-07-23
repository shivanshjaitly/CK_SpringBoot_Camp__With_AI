package in.codekerdos.booking.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares the booking.events topic so Spring Kafka's auto-configured
 * KafkaAdmin creates it on broker startup — no manual `kafka-topics.sh` step.
 * Gated behind app.kafka.enabled: registering a NewTopic bean makes KafkaAdmin
 * try to reach a broker during context startup, which would make every test
 * run wait out a connection timeout with no broker present.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", matchIfMissing = true)
public class KafkaTopicConfig {

    @Bean
    public NewTopic bookingEventsTopic() {
        return TopicBuilder.name("booking.events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
