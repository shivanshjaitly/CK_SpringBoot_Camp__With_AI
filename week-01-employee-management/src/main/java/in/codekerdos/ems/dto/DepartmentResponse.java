package in.codekerdos.ems.dto;

import in.codekerdos.ems.entity.Department;

public record DepartmentResponse(Long id, String name) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(department.getId(), department.getName());
    }
}
