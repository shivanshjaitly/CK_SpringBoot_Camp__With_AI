package in.codekerdos.expense.dto;

import in.codekerdos.expense.entity.Expense;
import in.codekerdos.expense.enums.ExpenseCategory;
import in.codekerdos.expense.enums.ExpenseStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        String title,
        String description,
        BigDecimal amount,
        LocalDate expenseDate,
        ExpenseStatus status,
        ExpenseCategory category,
        String submittedByEmail,
        String reviewedByEmail,
        String rejectionReason,
        String aiFraudFlags,
        Instant createdAt
) {
    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getTitle(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getStatus(),
                expense.getCategory(),
                expense.getSubmittedBy().getEmail(),
                expense.getReviewedBy() != null ? expense.getReviewedBy().getEmail() : null,
                expense.getRejectionReason(),
                expense.getAiFraudFlags(),
                expense.getCreatedAt()
        );
    }
}
