package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
@Transactional
public interface ClarificationRequestRepository extends JpaRepository<ClarificationRequest, Integer> {
    @Query(value = "SELECT MAX(key) FROM clarification_requests", nativeQuery = true)
    Integer getMaxClarificationRequestKey();

    @Query(value = "SELECT * FROM clarification_requests cr WHERE cr.user_id = :sId AND cr.question_id = :qId", nativeQuery = true)
    Optional<ClarificationRequest> getByStudentQuestion(int sId, int qId);

    @Query(value = "SELECT * from clarification_requests cr LEFT JOIN clarification_request_answers ca ON cr.id = ca.request_id" +
            "WHERE ca.request_id IS NULL", nativeQuery = true)
    Stream<ClarificationRequest> getUnansweredRequests();
}