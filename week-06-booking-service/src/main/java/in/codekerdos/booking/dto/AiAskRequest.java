package in.codekerdos.booking.dto;

import jakarta.validation.constraints.NotBlank;

public record AiAskRequest(@NotBlank String question) {
}
