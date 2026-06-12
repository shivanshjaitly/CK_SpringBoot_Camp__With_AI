package in.codekerdos.demo.service;

import in.codekerdos.demo.sender.MessageSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final MessageSender messageSender;

    public NotificationService(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public void notifyUser(String email, String message) {
        messageSender.send(email, message);
    }
}
