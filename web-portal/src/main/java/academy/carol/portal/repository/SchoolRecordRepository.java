package academy.carol.portal.repository;

import academy.carol.portal.domain.SchoolRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRecordRepository extends JpaRepository<SchoolRecord, Long> {
    List<SchoolRecord> findBySectionOrderByCreatedAtDesc(String section);
}
