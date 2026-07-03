package in.codekerdos.expense.config;

import in.codekerdos.expense.entity.AppUser;
import in.codekerdos.expense.enums.Role;
import in.codekerdos.expense.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner seedUsers(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!appUserRepository.existsByEmail("employee@codekerdos.in")) {
                AppUser employee = new AppUser();
                employee.setEmail("employee@codekerdos.in");
                employee.setPassword(passwordEncoder.encode("emp123"));
                employee.setFullName("Demo Employee");
                employee.setRole(Role.EMPLOYEE);
                appUserRepository.save(employee);
            }

            if (!appUserRepository.existsByEmail("manager@codekerdos.in")) {
                AppUser manager = new AppUser();
                manager.setEmail("manager@codekerdos.in");
                manager.setPassword(passwordEncoder.encode("mgr123"));
                manager.setFullName("Demo Manager");
                manager.setRole(Role.MANAGER);
                appUserRepository.save(manager);
            }

            if (!appUserRepository.existsByEmail("admin@codekerdos.in")) {
                AppUser admin = new AppUser();
                admin.setEmail("admin@codekerdos.in");
                admin.setPassword(passwordEncoder.encode("adm123"));
                admin.setFullName("Demo Admin");
                admin.setRole(Role.ADMIN);
                appUserRepository.save(admin);
            }
        };
    }
}
