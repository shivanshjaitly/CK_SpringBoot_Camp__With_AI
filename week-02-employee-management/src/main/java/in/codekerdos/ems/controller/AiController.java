package in.codekerdos.ems.controller;

import in.codekerdos.ems.dto.NaturalLanguageSearchRequest;
import in.codekerdos.ems.dto.NaturalLanguageSearchResponse;
import in.codekerdos.ems.dto.OnboardingRequest;
import in.codekerdos.ems.service.ai.AiEmployeeSearchService;
import in.codekerdos.ems.service.ai.AiGreetingService;
import in.codekerdos.ems.service.ai.AiOnboardingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiGreetingService aiGreetingService;
    private final AiOnboardingService aiOnboardingService;
    private final AiEmployeeSearchService aiEmployeeSearchService;

    public AiController(
            AiGreetingService aiGreetingService,
            AiOnboardingService aiOnboardingService,
            AiEmployeeSearchService aiEmployeeSearchService
    ) {
        this.aiGreetingService = aiGreetingService;
        this.aiOnboardingService = aiOnboardingService;
        this.aiEmployeeSearchService = aiEmployeeSearchService;
    }

    @GetMapping("/greet")
    public Map<String, String> greet(@RequestParam(defaultValue = "Student") String name) {
        String message = aiGreetingService.greet(name);
        return Map.of("name", name, "aiMessage", message);
    }

    @PostMapping("/onboarding-checklist")
    public Map<String, String> onboardingChecklist(@RequestBody @Valid OnboardingRequest request) {
        String checklist = aiOnboardingService.generateChecklist(request);
        return Map.of("employee", request.name(), "checklist", checklist);
    }

    @PostMapping("/search-employees")
    public NaturalLanguageSearchResponse searchEmployees(@RequestBody @Valid NaturalLanguageSearchRequest request) {
        return aiEmployeeSearchService.search(request);
    }
}
