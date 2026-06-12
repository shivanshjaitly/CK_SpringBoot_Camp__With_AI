package in.codekerdos.ems.dto;

import in.codekerdos.ems.entity.Employee;

import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String name,
        String role,
        String team,
        LocalDate joinedDate,
        String departmentName
) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getRole(),
                employee.getTeam(),
                employee.getJoinedDate(),
                employee.getDepartment() != null ? employee.getDepartment().getName() : null
        );
    }
}
