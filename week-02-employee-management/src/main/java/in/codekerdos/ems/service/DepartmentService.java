package in.codekerdos.ems.service;

import in.codekerdos.ems.dto.CreateDepartmentRequest;
import in.codekerdos.ems.dto.DepartmentResponse;
import in.codekerdos.ems.entity.Department;
import in.codekerdos.ems.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public DepartmentResponse create(CreateDepartmentRequest request) {
        Department department = new Department();
        department.setName(request.name());
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }
}
