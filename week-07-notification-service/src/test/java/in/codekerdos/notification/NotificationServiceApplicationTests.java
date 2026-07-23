package in.codekerdos.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "app.kafka-listener.enabled=false")
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
