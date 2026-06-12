package in.codekerdos.ems.service;

import in.codekerdos.ems.dto.CreateEmployeeRequest;
import in.codekerdos.ems.dto.EmployeeResponse;
import in.codekerdos.ems.entity.Department;
import in.codekerdos.ems.entity.Employee;
import in.codekerdos.ems.repository.EmployeeRepository;
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
}
