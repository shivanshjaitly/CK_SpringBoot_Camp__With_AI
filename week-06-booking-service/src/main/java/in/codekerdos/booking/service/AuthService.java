package in.codekerdos.booking.service;

import in.codekerdos.booking.dto.AuthResponse;
import in.codekerdos.booking.dto.LoginRequest;
import in.codekerdos.booking.dto.RegisterRequest;
import in.codekerdos.booking.entity.AppUser;
import in.codekerdos.booking.enums.Role;
import in.codekerdos.booking.exception.BusinessException;
import in.codekerdos.booking.repository.AppUserRepository;
import in.codekerdos.booking.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already registered");
        }
        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
        return tokenFor(user);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));
        return tokenFor(user);
    }

    private AuthResponse tokenFor(AppUser user) {
        String token = jwtService.generateToken(user.getEmail(), Map.of("role", user.getRole().name()));
        return new AuthResponse(token, user.getEmail(), user.getFullName(), user.getRole());
    }
}
