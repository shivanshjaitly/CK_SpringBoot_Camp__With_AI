package in.codekerdos.booking.controller;

import in.codekerdos.booking.dto.BookingResponse;
import in.codekerdos.booking.dto.CreateBookingRequest;
import in.codekerdos.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Book a slot (optional Idempotency-Key header — required in Class 2 demos)")
    public ResponseEntity<BookingResponse> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.create(request, authentication.getName(), idempotencyKey));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "List my bookings")
    public ResponseEntity<List<BookingResponse>> mine(Authentication authentication) {
        return ResponseEntity.ok(bookingService.findMine(authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all bookings (ADMIN)")
    public ResponseEntity<List<BookingResponse>> all() {
        return ResponseEntity.ok(bookingService.findAll());
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel my booking")
    public ResponseEntity<BookingResponse> cancel(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(bookingService.cancel(id, authentication.getName()));
    }
}
