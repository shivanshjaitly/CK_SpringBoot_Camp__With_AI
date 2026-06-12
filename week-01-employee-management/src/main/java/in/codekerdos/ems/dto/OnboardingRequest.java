package in.codekerdos.ems.dto;

import jakarta.validation.constraints.NotBlank;

public record OnboardingRequest(
        @NotBlank String name,
        @NotBlank String role,
        @NotBlank String department,
        @NotBlank String team,
        String joinedDate
) {
}
