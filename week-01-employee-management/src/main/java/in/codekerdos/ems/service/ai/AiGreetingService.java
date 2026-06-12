package in.codekerdos.ems.service.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiGreetingService {

    private final ChatClient chatClient;

    public AiGreetingService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String greet(String name) {
        return chatClient
                .prompt()
                .user("Say a warm one-line welcome to " + name
                        + " who just joined CodeKerdos Spring Boot + AI Bootcamp. Keep it under 20 words.")
                .call()
                .content();
    }
}
