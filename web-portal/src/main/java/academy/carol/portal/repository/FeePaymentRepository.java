package academy.carol.portal.repository;

import academy.carol.portal.domain.FeePayment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {
    List<FeePayment> findByFeeAccount_IdOrderByCreatedAtDesc(Long feeAccountId);
}
