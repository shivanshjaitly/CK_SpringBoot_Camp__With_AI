package in.codekerdos.booking.config;

import in.codekerdos.booking.entity.AppUser;
import in.codekerdos.booking.entity.Slot;
import in.codekerdos.booking.enums.ResourceType;
import in.codekerdos.booking.enums.Role;
import in.codekerdos.booking.enums.SlotStatus;
import in.codekerdos.booking.repository.AppUserRepository;
import in.codekerdos.booking.repository.SlotRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner seedUsersAndSlots(
            AppUserRepository userRepository,
            SlotRepository slotRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (!userRepository.existsByEmail("customer@codekerdos.in")) {
                AppUser customer = new AppUser();
                customer.setEmail("customer@codekerdos.in");
                customer.setPassword(passwordEncoder.encode("cust123"));
                customer.setFullName("Demo Customer");
                customer.setRole(Role.CUSTOMER);
                userRepository.save(customer);
            }
            if (!userRepository.existsByEmail("provider@codekerdos.in")) {
                AppUser provider = new AppUser();
                provider.setEmail("provider@codekerdos.in");
                provider.setPassword(passwordEncoder.encode("prov123"));
                provider.setFullName("Demo Provider");
                provider.setRole(Role.PROVIDER);
                userRepository.save(provider);
            }
            if (!userRepository.existsByEmail("admin@codekerdos.in")) {
                AppUser admin = new AppUser();
                admin.setEmail("admin@codekerdos.in");
                admin.setPassword(passwordEncoder.encode("adm123"));
                admin.setFullName("Demo Admin");
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
            }

            if (slotRepository.count() == 0) {
                AppUser provider = userRepository.findByEmail("provider@codekerdos.in").orElseThrow();
                Slot room = new Slot();
                room.setTitle("Quiet downtown meeting room");
                room.setDescription("Peaceful private meeting space for one-on-one discussions near downtown, whiteboard included.");
                room.setResourceType(ResourceType.MEETING_ROOM);
                room.setStartTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0));
                room.setEndTime(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0).withSecond(0).withNano(0));
                room.setLocation("Downtown Hub, Floor 3");
                room.setCapacity(4);
                room.setBookedCount(0);
                room.setStatus(SlotStatus.OPEN);
                room.setProvider(provider);
                slotRepository.save(room);

                Slot doctor = new Slot();
                doctor.setTitle("Dr. Mehta — follow-up consultation");
                doctor.setDescription("General physician follow-up slot. Calm clinic environment, good for post-treatment check-ins.");
                doctor.setResourceType(ResourceType.DOCTOR);
                doctor.setStartTime(LocalDateTime.now().plusDays(2).withHour(15).withMinute(0).withSecond(0).withNano(0));
                doctor.setEndTime(LocalDateTime.now().plusDays(2).withHour(15).withMinute(30).withSecond(0).withNano(0));
                doctor.setLocation("Care Clinic Block B");
                doctor.setCapacity(1);
                doctor.setBookedCount(0);
                doctor.setStatus(SlotStatus.OPEN);
                doctor.setProvider(provider);
                slotRepository.save(doctor);
            }
        };
    }
}
