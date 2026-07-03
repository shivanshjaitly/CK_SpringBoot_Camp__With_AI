package in.codekerdos.expense.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SubmitExpenseRequest(
        @NotBlank(message = "Title is required") String title,
        String description,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be positive") BigDecimal amount,
        @NotNull LocalDate expenseDate
) {}
