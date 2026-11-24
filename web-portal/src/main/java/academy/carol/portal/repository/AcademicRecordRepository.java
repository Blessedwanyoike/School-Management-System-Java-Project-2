package academy.carol.portal.repository;

import academy.carol.portal.domain.AcademicRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, Long> {
    boolean existsByStudent_Id(Long studentId);

    @Query("select record from AcademicRecord record join fetch record.student order by record.term, record.subject")
    List<AcademicRecord> findAllWithStudent();

    @Query("select record from AcademicRecord record join fetch record.student "
            + "where record.student.id = :studentId order by record.term, record.subject")
    List<AcademicRecord> findForStudentWithStudent(@Param("studentId") Long studentId);

    @Query("select record from AcademicRecord record join fetch record.student "
            + "where record.subject = :subject order by record.term, record.subject")
    List<AcademicRecord> findForSubjectWithStudent(@Param("subject") String subject);
}
