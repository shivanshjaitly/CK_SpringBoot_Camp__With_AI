package in.codekerdos.booking.dto;

import in.codekerdos.booking.enums.Role;

public record AuthResponse(
        String token,
        String email,
        String fullName,
        Role role
) {}
