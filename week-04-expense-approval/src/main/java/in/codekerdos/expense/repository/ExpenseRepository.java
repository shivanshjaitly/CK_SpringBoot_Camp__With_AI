package in.codekerdos.expense.repository;

import in.codekerdos.expense.entity.AppUser;
import in.codekerdos.expense.entity.Expense;
import in.codekerdos.expense.enums.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findBySubmittedByOrderByCreatedAtDesc(AppUser submittedBy);

    List<Expense> findByStatusOrderByCreatedAtAsc(ExpenseStatus status);

    List<Expense> findByExpenseDateBetween(LocalDate from, LocalDate to);
}
