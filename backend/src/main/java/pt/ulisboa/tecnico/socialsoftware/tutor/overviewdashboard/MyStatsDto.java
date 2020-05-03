package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;

import java.io.Serializable;

public class MyStatsDto implements Serializable {
    private MyStats.StatsVisibility testStatVisibility;
    private Integer testStatValue = 0;

    public MyStatsDto(MyStats myStats) {
        this.testStatVisibility = myStats.getTestStat();
    }

    public MyStats.StatsVisibility getTestStatVisibility() {
        return testStatVisibility;
    }

    public void setTestStatVisibility(Integer value) {this.testStatValue = value;}
}
