package in.codekerdos.ems.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmployeeSearchCriteria(
        String roleContains,
        String team,
        String departmentName,
        LocalDate joinedAfter,
        LocalDate joinedBefore
) {
}
