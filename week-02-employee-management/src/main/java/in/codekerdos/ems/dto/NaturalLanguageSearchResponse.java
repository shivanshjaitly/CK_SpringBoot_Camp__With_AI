package in.codekerdos.ems.dto;

import java.util.List;

public record NaturalLanguageSearchResponse(
        String query,
        EmployeeSearchCriteria parsedCriteria,
        PagedEmployeeResponse results
) {
}
