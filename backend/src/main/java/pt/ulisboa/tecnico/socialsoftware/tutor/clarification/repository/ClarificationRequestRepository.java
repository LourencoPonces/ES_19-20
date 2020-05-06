package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ClarificationRequestRepository extends JpaRepository<ClarificationRequest, Integer> {
    @Query(value = "SELECT MAX(key) FROM clarification_requests", nativeQuery = true)
    Integer getMaxClarificationRequestKey();

    @Query(value = "SELECT * FROM clarification_requests cr WHERE cr.user_id = :sId AND cr.question_id = :qId", nativeQuery = true)
    Optional<ClarificationRequest> getByStudentQuestion(int sId, int qId);

    @Query(value = "SELECT DISTINCT cr.* from clarification_requests cr JOIN users_course_executions uce ON uce.users_id = cr.creator_id WHERE uce.course_executions_id IN (SELECT uce2.course_executions_id FROM users_course_executions uce2 WHERE uce2.users_id = :tId)", nativeQuery = true)
    List<ClarificationRequest> getTeacherRequests(int tId);
}