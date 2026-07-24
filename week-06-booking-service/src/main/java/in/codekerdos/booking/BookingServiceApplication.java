package in.codekerdos.booking;

import org.springframework.ai.autoconfigure.transformers.TransformersEmbeddingModelAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// TransformersEmbeddingModelAutoConfiguration excluded — AiConfig defines the
// EmbeddingModel bean explicitly with Hugging Face model/tokenizer URIs
// instead of this autoconfiguration's GitHub-raw defaults.
@SpringBootApplication(exclude = TransformersEmbeddingModelAutoConfiguration.class)
@EnableScheduling
public class BookingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookingServiceApplication.class, args);
    }
}
