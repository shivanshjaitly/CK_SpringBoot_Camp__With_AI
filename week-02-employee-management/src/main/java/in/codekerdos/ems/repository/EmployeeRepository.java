package in.codekerdos.ems.repository;

import in.codekerdos.ems.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    List<Employee> findByTeam(String team);

    List<Employee> findByRoleContainingIgnoreCase(String role);

    List<Employee> findByJoinedDateAfter(LocalDate date);
}
