package in.codekerdos.booking.service.ai;

import in.codekerdos.booking.entity.Slot;
import in.codekerdos.booking.repository.SlotRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Keeps the vector store in sync with slots. The store only holds searchable
 * TEXT (title/description/location) keyed by slot id — live fields like
 * status and bookedCount are re-fetched from the DB at query time in
 * AiSearchService, so a stale embedding can never produce a stale answer
 * about whether a slot is still actually bookable.
 */
@Service
public class AiSlotIndexService {

    private final VectorStore vectorStore;
    private final SlotRepository slotRepository;

    public AiSlotIndexService(VectorStore vectorStore, SlotRepository slotRepository) {
        this.vectorStore = vectorStore;
        this.slotRepository = slotRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void indexAllOnStartup() {
        List<Document> documents = slotRepository.findAll().stream()
                .map(this::toDocument)
                .toList();
        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
    }

    /** Deterministic id = slot id, so re-indexing an existing slot overwrites rather than duplicates. */
    public void indexSlot(Slot slot) {
        vectorStore.add(List.of(toDocument(slot)));
    }

    private Document toDocument(Slot slot) {
        String text = slot.getTitle() + ". " + slot.getDescription()
                + " Located at " + slot.getLocation() + ". Resource type: " + slot.getResourceType() + ".";
        return new Document(String.valueOf(slot.getId()), text, Map.of("slotId", slot.getId()));
    }
}
