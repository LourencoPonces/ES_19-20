package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface MyStatsRepository extends JpaRepository<MyStats, Integer> {
    @Query(value = "select * from user_stats where user_id = :userId", nativeQuery = true)
    MyStats findByUserId(Integer userId);

}
