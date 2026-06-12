package in.codekerdos.ems.repository;

import in.codekerdos.ems.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByTeam(String team);

    List<Employee> findByRoleContainingIgnoreCase(String role);

    List<Employee> findByJoinedDateAfter(LocalDate date);
}
