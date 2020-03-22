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
    @Query(value = "SELECT * FROM tournaments t WHERE t.course_execution_id = :executionId", nativeQuery = true)
    List<Tournament> findTournaments(int executionId);

    @Query(value = "SELECT * FROM tournaments t WHERE t.course_execution_id = :executionId AND t.creation_date < now() AND now() < t.available_date AND NOT t.is_cancelled", nativeQuery = true)
    List<Tournament> findCreatedTournaments(int executionId);

    @Query(value = "SELECT * FROM tournaments t WHERE t.course_execution_id = :executionId AND t.available_date <= now() AND now() < t.running_date AND NOT t.is_cancelled", nativeQuery = true)
    List<Tournament> findAvailableTournaments(int executionId);

    @Query(value = "SELECT * FROM tournaments t WHERE t.course_execution_id = :executionId AND t.running_date < now() AND now() < t.conclusion_date AND NOT t.is_cancelled", nativeQuery = true)
    List<Tournament> findRunningTournaments(int executionId);

    @Query(value = "SELECT * FROM tournaments t WHERE t.course_execution_id = :executionId AND t.conclusion_date < now() AND NOT t.is_cancelled", nativeQuery = true)
    List<Tournament> findFinishedTournaments(int executionId);

    @Query(value = "SELECT * FROM tournaments t WHERE t.course_execution_id = :executionId AND t.is_cancelled", nativeQuery = true)
    List<Tournament> findCanceledTournaments(int executionId);

    @Query(value = "SELECT MAX(key) FROM tournaments", nativeQuery = true)
    Integer getMaxTournamentKey();
}
