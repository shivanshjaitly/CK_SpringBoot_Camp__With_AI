package in.codekerdos.expense.controller;

import in.codekerdos.expense.service.ai.AiManagerSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiManagerSummaryService aiManagerSummaryService;

    public AiController(AiManagerSummaryService aiManagerSummaryService) {
        this.aiManagerSummaryService = aiManagerSummaryService;
    }

    @GetMapping("/manager-summary")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<String> managerSummary() {
        return ResponseEntity.ok(aiManagerSummaryService.generateSummary());
    }
}
