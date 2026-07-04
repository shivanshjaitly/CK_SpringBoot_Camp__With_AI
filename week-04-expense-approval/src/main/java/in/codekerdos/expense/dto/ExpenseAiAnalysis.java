package in.codekerdos.expense.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.codekerdos.expense.enums.ExpenseCategory;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExpenseAiAnalysis(
        ExpenseCategory category,
        String fraudFlags,
        double confidence,
        String reasoning
) {}
