package in.codekerdos.expense.dto;

import in.codekerdos.expense.enums.ExpenseCategory;
import in.codekerdos.expense.enums.ExpenseStatus;

import java.math.BigDecimal;
import java.util.Map;

public record ExpenseSummaryResponse(
        BigDecimal totalAmount,
        long totalCount,
        Map<ExpenseStatus, Long> countByStatus,
        Map<ExpenseCategory, Long> countByCategory
) {}
