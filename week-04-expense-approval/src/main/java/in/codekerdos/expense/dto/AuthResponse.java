package in.codekerdos.expense.dto;

public record AuthResponse(
        String token,
        String email,
        String role,
        long expiresInMs
) {}
