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
    List<Tournament> findTournament(int executionId);

    @Query(value = "select * from tournaments t where t.course_execution_id = :executionId and t.creation_date < TIMESTAMP and TIMESTAMP < t.available_date", nativeQuery = true)
    List<Tournament> findCratedTournament(int executionId);

    @Query(value = "select * from tournaments t where t.course_execution_id = :executionId and t.available_date < TIMESTAMP and TIMESTAMP < t.running_date", nativeQuery = true)
    List<Tournament> findAvailableTournament(int executionId);

    @Query(value = "select * from tournaments t where t.course_execution_id = :executionId and t.running_date < TIMESTAMP and TIMESTAMP < t.conclusion_date", nativeQuery = true)
    List<Tournament> findRunningTournament(int executionId);

    @Query(value = "select * from tournaments t where t.course_execution_id = :executionId and t.conclusion_date < TIMESTAMP", nativeQuery = true)
    List<Tournament> findFinishedTournament(int executionId);

    @Query(value = "select * from tournaments t where t.course_execution_id = :executionId and t.isCancelled = TRUE", nativeQuery = true)
    List<Tournament> findCanceledTournament(int executionId);
}
