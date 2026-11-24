package academy.carol.portal.repository;

import academy.carol.portal.domain.SchoolAsset;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolAssetRepository extends JpaRepository<SchoolAsset, Long> {
    List<SchoolAsset> findAllByOrderByCategoryAscLocationAscItemNameAsc();
}
