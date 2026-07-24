package in.codekerdos.booking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "app.kafka.enabled=false",
        // ChatModel autoconfiguration validates the key is non-blank at bean
        // creation time, even though contextLoads() never actually calls Groq.
        "spring.ai.openai.api-key=test-key-for-context-load-only"
})
class BookingServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
