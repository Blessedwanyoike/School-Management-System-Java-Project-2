package academy.carol.portal.repository;

import academy.carol.portal.domain.Student;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentCode(String studentCode);

    @Query("select student from Student student "
            + "where lower(student.studentCode) = lower(:term) "
            + "or lower(student.fullName) like lower(concat('%', :term, '%')) "
            + "order by student.studentCode")
    List<Student> searchByAdmissionOrName(@Param("term") String term, Pageable pageable);
}
