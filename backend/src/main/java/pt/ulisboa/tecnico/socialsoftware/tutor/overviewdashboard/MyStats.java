package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;

import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;


@Entity
@Table(name = "user_stats")
public class MyStats implements DomainEntity {

    public enum StatsVisibility {
        PUBLIC, PRIVATE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    // All stats
    private StatsVisibility testStat;

    public MyStats() {}

    public MyStats(StatsVisibility statsVisibility) {
        this.testStat = statsVisibility;
    }

    public StatsVisibility getTestStat() {
        return testStat;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitMyStats(this);
    }

}
