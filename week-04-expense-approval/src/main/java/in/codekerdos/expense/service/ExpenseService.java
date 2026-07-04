package in.codekerdos.expense.service;

import in.codekerdos.expense.dto.ExpenseResponse;
import in.codekerdos.expense.dto.ExpenseSummaryResponse;
import in.codekerdos.expense.dto.SubmitExpenseRequest;
import in.codekerdos.expense.entity.AppUser;
import in.codekerdos.expense.entity.Expense;
import in.codekerdos.expense.enums.ExpenseCategory;
import in.codekerdos.expense.enums.ExpenseStatus;
import in.codekerdos.expense.repository.AppUserRepository;
import in.codekerdos.expense.repository.ExpenseRepository;
import in.codekerdos.expense.service.ai.AsyncExpenseAiProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AppUserRepository appUserRepository;
    private final AsyncExpenseAiProcessor asyncExpenseAiProcessor;

    public ExpenseService(ExpenseRepository expenseRepository,
                          AppUserRepository appUserRepository,
                          AsyncExpenseAiProcessor asyncExpenseAiProcessor) {
        this.expenseRepository = expenseRepository;
        this.appUserRepository = appUserRepository;
        this.asyncExpenseAiProcessor = asyncExpenseAiProcessor;
    }

    @Transactional
    public ExpenseResponse submit(SubmitExpenseRequest request, String userEmail) {
        AppUser user = getUserOrThrow(userEmail);

        Expense expense = new Expense();
        expense.setTitle(request.title());
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());
        expense.setStatus(ExpenseStatus.PENDING);
        expense.setSubmittedBy(user);

        Expense saved = expenseRepository.save(expense);
        asyncExpenseAiProcessor.processExpenseAsync(saved.getId());

        return ExpenseResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findMine(String userEmail) {
        AppUser user = getUserOrThrow(userEmail);
        return expenseRepository.findBySubmittedByOrderByCreatedAtDesc(user).stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findPending() {
        return expenseRepository.findByStatusOrderByCreatedAtAsc(ExpenseStatus.PENDING).stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @Transactional
    public ExpenseResponse approve(Long expenseId, String reviewerEmail) {
        Expense expense = getExpenseOrThrow(expenseId);
        AppUser reviewer = getUserOrThrow(reviewerEmail);

        assertStatus(expense, ExpenseStatus.PENDING);
        assertNotOwnExpense(expense, reviewer);

        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setReviewedBy(reviewer);

        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse reject(Long expenseId, String reason, String reviewerEmail) {
        Expense expense = getExpenseOrThrow(expenseId);
        AppUser reviewer = getUserOrThrow(reviewerEmail);

        assertStatus(expense, ExpenseStatus.PENDING);
        assertNotOwnExpense(expense, reviewer);

        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setReviewedBy(reviewer);
        expense.setRejectionReason(reason);

        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> findAll() {
        return expenseRepository.findAll().stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseSummaryResponse getSummary(LocalDate from, LocalDate to) {
        List<Expense> expenses = expenseRepository.findByExpenseDateBetween(from, to);

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<ExpenseStatus, Long> byStatus = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getStatus, Collectors.counting()));

        Map<ExpenseCategory, Long> byCategory = expenses.stream()
                .filter(e -> e.getCategory() != null)
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.counting()));

        return new ExpenseSummaryResponse(total, expenses.size(), byStatus, byCategory);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private Expense getExpenseOrThrow(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
    }

    private AppUser getUserOrThrow(String email) {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private void assertStatus(Expense expense, ExpenseStatus required) {
        if (expense.getStatus() != required) {
            throw new BusinessRuleException(
                    "Expense is already " + expense.getStatus() + " — cannot change");
        }
    }

    private void assertNotOwnExpense(Expense expense, AppUser reviewer) {
        if (expense.getSubmittedBy().getId().equals(reviewer.getId())) {
            throw new BusinessRuleException("Cannot approve or reject your own expense");
        }
    }
}
