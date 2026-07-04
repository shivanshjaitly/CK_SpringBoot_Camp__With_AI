package in.codekerdos.expense.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncExpenseAiProcessor {

    private static final Logger log = LoggerFactory.getLogger(AsyncExpenseAiProcessor.class);

    private final AiExpenseAnalysisService aiExpenseAnalysisService;

    public AsyncExpenseAiProcessor(AiExpenseAnalysisService aiExpenseAnalysisService) {
        this.aiExpenseAnalysisService = aiExpenseAnalysisService;
    }

    @Async
    public void processExpenseAsync(Long expenseId) {
        log.info("Starting async AI analysis for expense {}", expenseId);
        aiExpenseAnalysisService.analyzeAndUpdate(expenseId);
        log.info("Async AI analysis done for expense {}", expenseId);
    }
}
