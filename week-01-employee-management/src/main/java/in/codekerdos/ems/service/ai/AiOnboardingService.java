package in.codekerdos.ems.service.ai;

import in.codekerdos.ems.dto.OnboardingRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AiOnboardingService {

    private final ChatClient chatClient;

    private static final PromptTemplate ONBOARDING_TEMPLATE = new PromptTemplate("""
            You are an HR assistant at a tech company.
            Generate a concise onboarding checklist (5 bullet points) for:
            - Name: {name}
            - Role: {role}
            - Department: {department}
            - Team: {team}

            Include: tools to set up, people to meet, first-week goals.
            Format as plain text bullet points.
            """);

    public AiOnboardingService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String generateChecklist(OnboardingRequest request) {
        Prompt prompt = ONBOARDING_TEMPLATE.create(Map.of(
                "name", request.name(),
                "role", request.role(),
                "department", request.department(),
                "team", request.team()
        ));

        return chatClient.prompt(prompt).call().content();
    }
}
