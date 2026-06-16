package in.codekerdos.ems.controller;

import in.codekerdos.ems.dto.CreateDepartmentRequest;
import in.codekerdos.ems.dto.DepartmentResponse;
import in.codekerdos.ems.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponse create(@RequestBody @Valid CreateDepartmentRequest request) {
        return departmentService.create(request);
    }

    @GetMapping
    public List<DepartmentResponse> getAll() {
        return departmentService.findAll();
    }
}
