package academy.carol.portal.repository;

import academy.carol.portal.domain.BusAssignment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusAssignmentRepository extends JpaRepository<BusAssignment, Long> {
    Optional<BusAssignment> findByDriverUsername(String driverUsername);
}
