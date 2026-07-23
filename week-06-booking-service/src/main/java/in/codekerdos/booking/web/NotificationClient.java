package in.codekerdos.booking.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Reads notification-service's REST API for the dashboard — a real cross-service
 * HTTP call, separate from the Kafka event that got the notification created in
 * the first place. If notification-service is down, the booking UI still works;
 * this just shows an empty feed instead of failing the whole page.
 */
@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestClient restClient;

    public NotificationClient(@Value("${app.notification-service.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    public List<NotificationView> recentFor(String customerEmail) {
        try {
            List<NotificationView> all = restClient.get()
                    .uri("/api/notifications")
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<NotificationView>>() {});
            return all == null ? List.of() : all.stream()
                    .filter(n -> customerEmail.equals(n.customerEmail()))
                    .toList();
        } catch (Exception ex) {
            log.warn("notification-service unreachable at dashboard render time: {}", ex.getMessage());
            return List.of();
        }
    }
}
