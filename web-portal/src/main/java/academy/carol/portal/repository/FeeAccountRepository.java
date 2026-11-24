package academy.carol.portal.repository;

import academy.carol.portal.domain.FeeAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeAccountRepository extends JpaRepository<FeeAccount, Long> {
    Optional<FeeAccount> findByStudent_Id(Long studentId);
}
