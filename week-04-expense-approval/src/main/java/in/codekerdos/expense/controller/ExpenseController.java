package in.codekerdos.expense.controller;

import in.codekerdos.expense.dto.ExpenseResponse;
import in.codekerdos.expense.dto.ExpenseSummaryResponse;
import in.codekerdos.expense.dto.RejectExpenseRequest;
import in.codekerdos.expense.dto.SubmitExpenseRequest;
import in.codekerdos.expense.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // ── EMPLOYEE endpoints ────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ExpenseResponse> submit(
            @RequestBody @Valid SubmitExpenseRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.submit(request, authentication.getName()));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ExpenseResponse>> mine(Authentication authentication) {
        return ResponseEntity.ok(expenseService.findMine(authentication.getName()));
    }

    // ── MANAGER endpoints ─────────────────────────────────────────────────────

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<ExpenseResponse>> pending() {
        return ResponseEntity.ok(expenseService.findPending());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ExpenseResponse> approve(@PathVariable Long id,
                                                   Authentication authentication) {
        return ResponseEntity.ok(expenseService.approve(id, authentication.getName()));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ExpenseResponse> reject(@PathVariable Long id,
                                                  @RequestBody @Valid RejectExpenseRequest request,
                                                  Authentication authentication) {
        return ResponseEntity.ok(expenseService.reject(id, request.reason(), authentication.getName()));
    }

    // ── ADMIN endpoint ────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExpenseResponse>> all() {
        return ResponseEntity.ok(expenseService.findAll());
    }

    // ── MANAGER + ADMIN ───────────────────────────────────────────────────────

    @GetMapping("/summary")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ExpenseSummaryResponse> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(expenseService.getSummary(from, to));
    }
}
