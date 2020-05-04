package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MyStatsDto implements Serializable {
    private Map<String, Integer> statsValues = new HashMap<>();
    private Map<String, MyStats.StatsVisibility> statsVisibility = new HashMap();

    public MyStatsDto() { }

    public MyStatsDto(MyStats myStats) {
        this.statsVisibility.put("requestsSubmitted", myStats.getRequestsSubmitted());
        this.statsVisibility.put("publicRequests", myStats.getPublicRequests());
    }

    public MyStats.StatsVisibility getRequestsSubmittedVisibility() {
        return this.statsVisibility.get("requestsSubmitted");
    }

    public void setRequestsSubmittedVisibility(MyStats.StatsVisibility visibility) {
        this.statsVisibility.put("requestsSubmitted", visibility);
    }

    public MyStats.StatsVisibility getPublicRequestsVisibility() {
        return this.statsVisibility.get("publicRequests");
    }

    public void setPublicRequestsVisibility(MyStats.StatsVisibility visibility) {
        this.statsVisibility.put("publicRequests", visibility);
    }

    public Integer getRequestsSubmittedStat() { return this.statsValues.get("requestsSubmitted"); }

    public void setRequestsSubmittedStat(Integer value) { this.statsValues.put("requestsSubmitted", value); }

    public Integer getPublicRequestsStat() { return this.statsValues.get("publicRequests"); }

    public void setPublicRequestsStat(Integer value) { this.statsValues.put("publicRequests", value); }
}
