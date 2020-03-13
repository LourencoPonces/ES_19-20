package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;

import java.util.List;

@Repository
@Transactional
public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
    @Query(value = "select * from tournaments t where t.course_execution_id = :executionId", nativeQuery = true)
    List<Tournament> findTournaments(int executionId);

    @Query(value = "select * from tournaments t where t.course_execution_id = :executionId and t.creation_date < now() and now() < t.available_date and not t.is_cancelled", nativeQuery = true)
    List<Tournament> findCreatedTournaments(int executionId);

    @Query(value = "select * from tournaments t where t.course_execution_id = :executionId and t.available_date <= now() and now() < t.running_date and not t.is_cancelled", nativeQuery = true)
    List<Tournament> findAvailableTournaments(int executionId);

    @Query(value = "select * from tournaments t where t.course_execution_id = :executionId and t.running_date < now() and now()  < t.conclusion_date and not t.is_cancelled", nativeQuery = true)
    List<Tournament> findRunningTournaments(int executionId);

    @Query(value = "select * from tournaments t where t.course_execution_id = :executionId and t.conclusion_date < now() and not t.is_cancelled", nativeQuery = true)
    List<Tournament> findFinishedTournaments(int executionId);

    @Query(value = "select * from tournaments t where t.course_execution_id = :executionId and t.is_cancelled", nativeQuery = true)
    List<Tournament> findCanceledTournaments(int executionId);
}
