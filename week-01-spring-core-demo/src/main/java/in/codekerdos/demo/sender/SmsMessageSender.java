package in.codekerdos.demo.sender;

import org.springframework.stereotype.Component;

/**
 * Homework: wire this via @Qualifier("smsMessageSender") in AlertService.
 */
@Component("smsMessageSender")
public class SmsMessageSender implements MessageSender {

    @Override
    public void send(String to, String message) {
        System.out.println("📱 SMS to " + to + ": " + message);
    }
}
