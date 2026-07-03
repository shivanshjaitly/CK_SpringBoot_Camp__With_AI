package in.codekerdos.ems.service;

import in.codekerdos.ems.dto.CreateEmployeeRequest;
import in.codekerdos.ems.exception.ResourceNotFoundException;
import in.codekerdos.ems.dto.EmployeeResponse;
import in.codekerdos.ems.dto.PagedEmployeeResponse;
import in.codekerdos.ems.entity.Department;
import in.codekerdos.ems.entity.Employee;
import in.codekerdos.ems.repository.EmployeeRepository;
import in.codekerdos.ems.specification.EmployeeSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentService departmentService;

    public EmployeeService(EmployeeRepository employeeRepository, DepartmentService departmentService) {
        this.employeeRepository = employeeRepository;
        this.departmentService = departmentService;
    }

    public EmployeeResponse create(CreateEmployeeRequest request) {
        Department department = departmentService.findById(request.departmentId());

        Employee employee = new Employee();
        employee.setName(request.name());
        employee.setRole(request.role());
        employee.setTeam(request.team());
        employee.setJoinedDate(request.joinedDate());
        employee.setDepartment(department);

        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll() {
        return employeeRepository.findAll().stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedEmployeeResponse findAllPaged(int page, int size, String sort, String team) {
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParts[0]));

        Page<Employee> result;
        if (team != null && !team.isBlank()) {
            Specification<Employee> spec = EmployeeSpecifications.hasTeam(team);
            result = employeeRepository.findAll(spec, pageable);
        } else {
            result = employeeRepository.findAll(pageable);
        }

        return toPagedResponse(result);
    }

    @Transactional(readOnly = true)
    public PagedEmployeeResponse searchByCriteria(
            in.codekerdos.ems.dto.EmployeeSearchCriteria criteria,
            int page,
            int size
    ) {
        Specification<Employee> spec = EmployeeSpecifications.fromCriteria(criteria);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "joinedDate"));
        Page<Employee> result = employeeRepository.findAll(spec, pageable);
        return toPagedResponse(result);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return EmployeeResponse.from(employee);
    }

    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    private PagedEmployeeResponse toPagedResponse(Page<Employee> page) {
        List<EmployeeResponse> content = page.getContent().stream()
                .map(EmployeeResponse::from)
                .toList();
        return new PagedEmployeeResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
