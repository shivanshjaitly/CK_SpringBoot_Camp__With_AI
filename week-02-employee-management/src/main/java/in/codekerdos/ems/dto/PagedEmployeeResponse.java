package in.codekerdos.ems.dto;

import java.util.List;

public record PagedEmployeeResponse(
        List<EmployeeResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
