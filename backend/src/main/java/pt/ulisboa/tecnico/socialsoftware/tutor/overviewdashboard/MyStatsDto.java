package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;

import java.io.Serializable;

public class MyStatsDto implements Serializable {
    private MyStats.StatsVisibility testStat;
    private Integer testStatValue = 0;

    public MyStatsDto(MyStats myStats) {
        this.testStat = myStats.getTestStat();
    }

    public MyStats.StatsVisibility getTestStat() {
        return testStat;
    }
}
