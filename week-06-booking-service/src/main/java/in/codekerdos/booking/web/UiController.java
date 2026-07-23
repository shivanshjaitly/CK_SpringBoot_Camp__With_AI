package in.codekerdos.booking.web;

import in.codekerdos.booking.dto.BookingResponse;
import in.codekerdos.booking.dto.CreateBookingRequest;
import in.codekerdos.booking.exception.BusinessException;
import in.codekerdos.booking.exception.ResourceNotFoundException;
import in.codekerdos.booking.service.BookingService;
import in.codekerdos.booking.service.SlotService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Server-rendered UI at /ui/** — a separate, session-based front door onto the
 * SAME service layer the JSON API (/api/**) uses. No REST calls between UI and
 * API within this service; only notification-service is reached over HTTP,
 * because that one really is a different process.
 */
@Controller
@RequestMapping("/ui")
public class UiController {

    private final SlotService slotService;
    private final BookingService bookingService;
    private final NotificationClient notificationClient;

    public UiController(SlotService slotService, BookingService bookingService, NotificationClient notificationClient) {
        this.slotService = slotService;
        this.bookingService = bookingService;
        this.notificationClient = notificationClient;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("slots", slotService.listOpen(null));
        return "dashboard";
    }

    @PostMapping("/bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String book(@RequestParam Long slotId,
                        @RequestParam(required = false) String notes,
                        @RequestParam String idempotencyKey,
                        Authentication authentication,
                        RedirectAttributes redirectAttributes) {
        try {
            BookingResponse booking = bookingService.create(
                    new CreateBookingRequest(slotId, notes), authentication.getName(), idempotencyKey);
            redirectAttributes.addFlashAttribute("message", "Booked \"" + booking.slotTitle() + "\" — status " + booking.status());
        } catch (BusinessException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/ui/dashboard";
    }

    @GetMapping("/bookings/mine")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String myBookings(Model model, Authentication authentication) {
        model.addAttribute("bookings", bookingService.findMine(authentication.getName()));
        model.addAttribute("notifications", notificationClient.recentFor(authentication.getName()));
        return "bookings";
    }

    @PostMapping("/bookings/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public String cancel(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancel(id, authentication.getName());
            redirectAttributes.addFlashAttribute("message", "Booking #" + id + " cancelled");
        } catch (BusinessException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/ui/bookings/mine";
    }
}
