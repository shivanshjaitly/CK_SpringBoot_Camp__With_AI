package in.codekerdos.ems.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateEmployeeRequest(
        @NotBlank(message = "Name is required")
        String name,
        String role,
        String team,
        LocalDate joinedDate,
        @NotNull(message = "Department ID is required")
        Long departmentId
) {
}
