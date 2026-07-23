package in.codekerdos.notification.controller;

import in.codekerdos.notification.dto.NotificationResponse;
import in.codekerdos.notification.repository.NotificationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** No auth on purpose — this is an internal service, never exposed to the public internet directly. */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public List<NotificationResponse> recent() {
        return notificationRepository.findTop50ByOrderBySentAtDesc().stream()
                .map(NotificationResponse::from)
                .toList();
    }
}
