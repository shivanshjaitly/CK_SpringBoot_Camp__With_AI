package in.codekerdos.booking.service.ai;

import in.codekerdos.booking.dto.SlotResponse;
import in.codekerdos.booking.entity.Slot;
import in.codekerdos.booking.enums.SlotStatus;
import in.codekerdos.booking.repository.SlotRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RAG: similaritySearch embeds the query and retrieves top-K slot documents
 * by MEANING, not keyword overlap ("quiet room for 1:1" finds the downtown
 * meeting room even with zero shared words). Each candidate's live DB row is
 * re-fetched before use — the vector store can go stale (slot booked out,
 * deleted), the database is always the source of truth.
 */
@Service
public class AiSearchService {

    private static final int TOP_K = 5;

    private final VectorStore vectorStore;
    private final SlotRepository slotRepository;
    private final ChatClient chatClient;

    public AiSearchService(VectorStore vectorStore, SlotRepository slotRepository, ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.slotRepository = slotRepository;
        this.chatClient = chatClientBuilder.build();
    }

    public List<SlotResponse> semanticSearch(String query) {
        return liveSlotsFor(query).stream()
                .filter(slot -> slot.getStatus() == SlotStatus.OPEN)
                .map(SlotResponse::from)
                .toList();
    }

    public String ask(String question) {
        List<Slot> liveSlots = liveSlotsFor(question);

        if (liveSlots.isEmpty()) {
            return "I couldn't find any slots matching that. Try describing what you're looking for differently.";
        }

        String context = liveSlots.stream()
                .map(slot -> "- \"" + slot.getTitle() + "\" (" + slot.getResourceType() + ") at " + slot.getLocation()
                        + ", " + slot.getStartTime() + " to " + slot.getEndTime()
                        + ", status: " + slot.getStatus() + ", " + slot.getBookedCount() + "/" + slot.getCapacity() + " booked. "
                        + slot.getDescription())
                .collect(Collectors.joining("\n"));

        String prompt = """
                You are a booking assistant. Answer the user's question using ONLY the slots listed below.
                If none of them fit, say so honestly — do not invent a slot that isn't listed.
                Mention the slot title and whether it's still bookable (status OPEN and not fully booked).

                Slots:
                %s

                Question: %s
                """.formatted(context, question);

        return chatClient.prompt().user(prompt).call().content();
    }

    private List<Slot> liveSlotsFor(String query) {
        List<Document> matches = vectorStore.similaritySearch(SearchRequest.query(query).withTopK(TOP_K));
        return matches.stream()
                .map(doc -> Long.valueOf(doc.getId()))
                .map(slotRepository::findByIdWithProvider)
                .flatMap(Optional::stream)
                .toList();
    }
}
