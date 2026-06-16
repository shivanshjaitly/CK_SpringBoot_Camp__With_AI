package in.codekerdos.ems.dto;

import jakarta.validation.constraints.NotBlank;

public record NaturalLanguageSearchRequest(
        @NotBlank(message = "Search query is required")
        String query,
        Integer page,
        Integer size
) {
}
