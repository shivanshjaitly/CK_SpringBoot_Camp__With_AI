package in.codekerdos.booking.controller;

import in.codekerdos.booking.dto.CreateSlotRequest;
import in.codekerdos.booking.dto.SlotResponse;
import in.codekerdos.booking.enums.ResourceType;
import in.codekerdos.booking.service.SlotService;
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
@RequestMapping("/api/slots")
@Tag(name = "Slots")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    @Operation(summary = "Create a slot (PROVIDER)")
    public ResponseEntity<SlotResponse> create(
            @Valid @RequestBody CreateSlotRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(slotService.create(request, authentication.getName()));
    }

    @GetMapping
    @Operation(summary = "List open slots (optional resourceType filter)")
    public ResponseEntity<List<SlotResponse>> list(@RequestParam(required = false) ResourceType resourceType) {
        return ResponseEntity.ok(slotService.listOpen(resourceType));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get slot by id")
    public ResponseEntity<SlotResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(slotService.get(id));
    }
}
