package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest;
import java.util.Optional;

@Repository
@Transactional
public interface ClarificationRequestRepository extends JpaRepository<ClarificationRequest, Integer> {
    @Query(value = "SELECT MAX(key) FROM clarification_requests", nativeQuery = true)
    Integer getMaxClarificationRequestKey();

    @Query(value = "SELECT * FROM clarification_requests cr WHERE cr.user_id = :sId and cr.question_id = :qId", nativeQuery = true)
    Optional<ClarificationRequest> getByStudentQuestion(int sId, int qId);
}