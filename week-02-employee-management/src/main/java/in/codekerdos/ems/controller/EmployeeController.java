package in.codekerdos.ems.controller;

import in.codekerdos.ems.dto.CreateEmployeeRequest;
import in.codekerdos.ems.dto.EmployeeResponse;
import in.codekerdos.ems.dto.PagedEmployeeResponse;
import in.codekerdos.ems.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public PagedEmployeeResponse getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "joinedDate,desc") String sort,
            @RequestParam(required = false) String team
    ) {
        return employeeService.findAllPaged(page, size, sort, team);
    }

    @GetMapping("/all")
    public List<EmployeeResponse> getAllUnpaged() {
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable Long id) {
        return employeeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@RequestBody @Valid CreateEmployeeRequest request) {
        return employeeService.create(request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
