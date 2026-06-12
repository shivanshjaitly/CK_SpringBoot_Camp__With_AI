package in.codekerdos.demo.sender;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class EmailMessageSender implements MessageSender {

    @Override
    public void send(String to, String message) {
        System.out.println("📧 Email to " + to + ": " + message);
    }
}
