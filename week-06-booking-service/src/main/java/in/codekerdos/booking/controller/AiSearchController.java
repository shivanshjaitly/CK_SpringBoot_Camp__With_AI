package in.codekerdos.booking.controller;

import in.codekerdos.booking.dto.AiAskRequest;
import in.codekerdos.booking.dto.AiAskResponse;
import in.codekerdos.booking.dto.SlotResponse;
import in.codekerdos.booking.service.ai.AiSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Search (RAG)")
public class AiSearchController {

    private final AiSearchService aiSearchService;

    public AiSearchController(AiSearchService aiSearchService) {
        this.aiSearchService = aiSearchService;
    }

    @GetMapping("/search")
    @Operation(summary = "Semantic slot search — matches by meaning, not keywords")
    public ResponseEntity<List<SlotResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(aiSearchService.semanticSearch(query));
    }

    @PostMapping("/ask")
    @Operation(summary = "RAG: ask a question, answered from live slot data via Groq")
    public ResponseEntity<AiAskResponse> ask(@Valid @RequestBody AiAskRequest request) {
        return ResponseEntity.ok(new AiAskResponse(aiSearchService.ask(request.question())));
    }
}
