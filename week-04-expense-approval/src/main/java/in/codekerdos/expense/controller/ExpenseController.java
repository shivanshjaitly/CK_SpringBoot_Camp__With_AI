package in.codekerdos.expense.controller;

import in.codekerdos.expense.dto.ExpenseResponse;
import in.codekerdos.expense.dto.SubmitExpenseRequest;
import in.codekerdos.expense.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

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
}
