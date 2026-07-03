package in.codekerdos.expense.service;

import in.codekerdos.expense.dto.AuthResponse;
import in.codekerdos.expense.dto.LoginRequest;
import in.codekerdos.expense.entity.AppUser;
import in.codekerdos.expense.repository.AppUserRepository;
import in.codekerdos.expense.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository appUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        Map<String, Object> extraClaims = Map.of("role", user.getRole().name());
        String token = jwtService.generateToken(user.getEmail(), extraClaims);

        return new AuthResponse(token, user.getEmail(), user.getRole().name(), jwtService.getExpirationMs());
    }
}
