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
    @Query(value = "select * from tounaments t where t.course_execution_id = :courseId", nativeQuery = true)
    List<Tournament> findTournament(int courseId);

    @Query(value = "select * from tounaments t where t.course_execution_id = :courseId and t.status ='CREATED'", nativeQuery = true)
    List<Tournament> findCratedTournament(int courseId);

    @Query(value = "select * from tounaments t where t.course_execution_id = :courseId and t.status ='AVAILABLE'", nativeQuery = true)
    List<Tournament> findAvailableTournament(int courseId);

    @Query(value = "select * from tounaments t where t.course_execution_id = :courseId and t.status ='RUNNING'", nativeQuery = true)
    List<Tournament> findRunningTournament(int courseId);

    @Query(value = "select * from tounaments t where t.course_execution_id = :courseId and t.status ='FINISHED'", nativeQuery = true)
    List<Tournament> findFinishedTournament(int courseId);

    @Query(value = "select * from tounaments t where t.course_execution_id = :courseId and t.status ='CANCELED'", nativeQuery = true)
    List<Tournament> findCanceledTournament(int courseId);
}
