package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;

import java.io.Serializable;

public class MyStatsDto implements Serializable {
    private MyStats.StatsVisibility testStatVisibility;
    private Integer testStat = 0;

    public MyStatsDto(MyStats myStats) {
        this.testStatVisibility = myStats.getTestStat();
    }

    public MyStats.StatsVisibility getTestStatVisibility() {
        return testStatVisibility;
    }

    public Integer getTestStat() {
        return testStat;
    }

    public void setTestStatValue(Integer value) {this.testStat = value;}

    public void setTestStatVisibility(MyStats.StatsVisibility testStatVisibility) {
        this.testStatVisibility = testStatVisibility;
    }
}
