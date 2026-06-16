package in.codekerdos.ems.specification;

import in.codekerdos.ems.dto.EmployeeSearchCriteria;
import in.codekerdos.ems.entity.Employee;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    public static Specification<Employee> hasTeam(String team) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("team")), team.toLowerCase());
    }

    public static Specification<Employee> fromCriteria(EmployeeSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.roleContains() != null && !criteria.roleContains().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("role")),
                        "%" + criteria.roleContains().toLowerCase() + "%"
                ));
            }

            if (criteria.team() != null && !criteria.team().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("team")), criteria.team().toLowerCase()));
            }

            if (criteria.departmentName() != null && !criteria.departmentName().isBlank()) {
                var departmentJoin = root.join("department", JoinType.LEFT);
                predicates.add(cb.equal(
                        cb.lower(departmentJoin.get("name")),
                        criteria.departmentName().toLowerCase()
                ));
            }

            if (criteria.joinedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("joinedDate"), criteria.joinedAfter()));
            }

            if (criteria.joinedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("joinedDate"), criteria.joinedBefore()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
