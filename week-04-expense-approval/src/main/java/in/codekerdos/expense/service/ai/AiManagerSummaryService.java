package in.codekerdos.expense.service.ai;

import in.codekerdos.expense.entity.Expense;
import in.codekerdos.expense.enums.ExpenseStatus;
import in.codekerdos.expense.repository.ExpenseRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiManagerSummaryService {

    private static final String SUMMARY_PROMPT = """
            You are an expense manager assistant. Write a 3-line plain-English summary of these pending expenses.
            Be concise. Flag anything suspicious. Use bullet points. No JSON.

            Pending expenses:
            {pendingExpenses}
            """;

    private final ChatClient chatClient;
    private final ExpenseRepository expenseRepository;

    public AiManagerSummaryService(ChatClient.Builder chatClientBuilder,
                                   ExpenseRepository expenseRepository) {
        this.chatClient = chatClientBuilder.build();
        this.expenseRepository = expenseRepository;
    }

    public String generateSummary() {
        List<Expense> pending = expenseRepository.findByStatusOrderByCreatedAtAsc(ExpenseStatus.PENDING);

        if (pending.isEmpty()) {
            return "No pending expenses at this time.";
        }

        String expenseList = pending.stream()
                .map(e -> "- %s | ₹%s | %s | fraud: %s".formatted(
                        e.getTitle(),
                        e.getAmount().toPlainString(),
                        e.getSubmittedBy().getEmail(),
                        e.getAiFraudFlags() != null ? e.getAiFraudFlags() : "not analysed"
                ))
                .reduce("", (a, b) -> a + "\n" + b);

        String filledPrompt = SUMMARY_PROMPT.replace("{pendingExpenses}", expenseList);

        return chatClient.prompt()
                .user(filledPrompt)
                .call()
                .content();
    }
}
