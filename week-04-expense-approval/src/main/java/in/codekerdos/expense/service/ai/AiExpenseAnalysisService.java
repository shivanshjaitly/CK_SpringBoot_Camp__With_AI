package in.codekerdos.expense.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.codekerdos.expense.dto.ExpenseAiAnalysis;
import in.codekerdos.expense.entity.Expense;
import in.codekerdos.expense.repository.ExpenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AiExpenseAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiExpenseAnalysisService.class);

    private static final String ANALYSIS_PROMPT = """
            You are an expense policy analyst. Analyze this expense and return ONLY valid JSON.
            No markdown, no explanation — just JSON.

            Expense details:
            - Title: {title}
            - Description: {description}
            - Amount (INR): {amount}
            - Recent expenses by same user: {recentExpenses}

            Return JSON in exactly this shape:
            {
              "category": "TRAVEL|FOOD|SOFTWARE|EQUIPMENT|OTHER",
              "fraudFlags": "NONE or comma-separated: DUPLICATE_AMOUNT, EXCEEDS_POLICY, SUSPICIOUS_DESCRIPTION",
              "confidence": 0.0 to 1.0,
              "reasoning": "one sentence"
            }
            """;

    private final ChatClient chatClient;
    private final ExpenseRepository expenseRepository;
    private final ObjectMapper objectMapper;

    public AiExpenseAnalysisService(ChatClient.Builder chatClientBuilder,
                                    ExpenseRepository expenseRepository,
                                    ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.expenseRepository = expenseRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void analyzeAndUpdate(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId).orElse(null);
        if (expense == null) return;

        String recentExpenses = expenseRepository
                .findBySubmittedByOrderByCreatedAtDesc(expense.getSubmittedBy())
                .stream()
                .limit(5)
                .map(e -> e.getTitle() + " ₹" + e.getAmount())
                .reduce("", (a, b) -> a + "\n" + b);

        String filledPrompt = ANALYSIS_PROMPT
                .replace("{title}", expense.getTitle())
                .replace("{description}", expense.getDescription() != null ? expense.getDescription() : "")
                .replace("{amount}", expense.getAmount().toPlainString())
                .replace("{recentExpenses}", recentExpenses.isBlank() ? "none" : recentExpenses);

        try {
            String rawResponse = chatClient.prompt()
                    .user(filledPrompt)
                    .call()
                    .content();

            String cleaned = stripMarkdownFences(rawResponse);
            ExpenseAiAnalysis analysis = objectMapper.readValue(cleaned, ExpenseAiAnalysis.class);

            expense.setCategory(analysis.category());
            expense.setAiFraudFlags(analysis.fraudFlags());
            expense.setAiProcessedAt(Instant.now());
            expenseRepository.save(expense);

            log.info("AI analysis complete for expense {}: category={}, flags={}",
                    expenseId, analysis.category(), analysis.fraudFlags());

        } catch (Exception e) {
            log.error("AI analysis failed for expense {}: {}", expenseId, e.getMessage());
        }
    }

    private String stripMarkdownFences(String response) {
        return response
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();
    }
}
