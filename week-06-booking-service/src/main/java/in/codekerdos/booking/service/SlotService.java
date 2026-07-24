package in.codekerdos.booking.service;

import in.codekerdos.booking.dto.CreateSlotRequest;
import in.codekerdos.booking.dto.SlotResponse;
import in.codekerdos.booking.entity.AppUser;
import in.codekerdos.booking.entity.Slot;
import in.codekerdos.booking.enums.ResourceType;
import in.codekerdos.booking.enums.SlotStatus;
import in.codekerdos.booking.exception.BusinessException;
import in.codekerdos.booking.exception.ResourceNotFoundException;
import in.codekerdos.booking.repository.AppUserRepository;
import in.codekerdos.booking.repository.SlotRepository;
import in.codekerdos.booking.service.ai.AiSlotIndexService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final AppUserRepository userRepository;
    private final AiSlotIndexService aiSlotIndexService;

    public SlotService(SlotRepository slotRepository, AppUserRepository userRepository,
                        AiSlotIndexService aiSlotIndexService) {
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.aiSlotIndexService = aiSlotIndexService;
    }

    @Transactional
    public SlotResponse create(CreateSlotRequest request, String providerEmail) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BusinessException("endTime must be after startTime");
        }
        AppUser provider = userRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found"));

        Slot slot = new Slot();
        slot.setTitle(request.title());
        slot.setDescription(request.description());
        slot.setResourceType(request.resourceType());
        slot.setStartTime(request.startTime());
        slot.setEndTime(request.endTime());
        slot.setLocation(request.location());
        slot.setCapacity(request.capacity());
        slot.setBookedCount(0);
        slot.setStatus(SlotStatus.OPEN);
        slot.setProvider(provider);
        Slot saved = slotRepository.save(slot);
        // Makes the slot semantically searchable immediately — not just after the
        // next full reindex on a future restart.
        aiSlotIndexService.indexSlot(saved);
        return SlotResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> listOpen(ResourceType type) {
        List<Slot> slots = type == null
                ? slotRepository.findByStatus(SlotStatus.OPEN)
                : slotRepository.findByResourceTypeAndStatus(type, SlotStatus.OPEN);
        return slots.stream().map(SlotResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SlotResponse get(Long id) {
        Slot slot = slotRepository.findByIdWithProvider(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found: " + id));
        return SlotResponse.from(slot);
    }
}
