package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MyStatsDto implements Serializable {
    private static final String REQUESTS_SUBMITTED = "requestsSubmitted";
    private static final String PUBLIC_REQUESTS = "publicRequests";
    private Map<String, Integer> statsValues = new HashMap<>();
    private Map<String, MyStats.StatsVisibility> statsVisibility = new HashMap<>();

    public MyStatsDto() { }

    public MyStatsDto(MyStats myStats) {
        this.statsVisibility.put(REQUESTS_SUBMITTED, myStats.getRequestsSubmitted());
        this.statsVisibility.put(PUBLIC_REQUESTS, myStats.getPublicRequests());
    }

    public MyStats.StatsVisibility getRequestsSubmittedVisibility() {
        return this.statsVisibility.get("requestsSubmitted");
    }

    public void setRequestsSubmittedVisibility(MyStats.StatsVisibility visibility) {
        this.statsVisibility.put(REQUESTS_SUBMITTED, visibility);
    }

    public MyStats.StatsVisibility getPublicRequestsVisibility() {
        return this.statsVisibility.get(PUBLIC_REQUESTS);
    }

    public void setPublicRequestsVisibility(MyStats.StatsVisibility visibility) {
        this.statsVisibility.put(PUBLIC_REQUESTS, visibility);
    }

    public Integer getRequestsSubmittedStat() { return this.statsValues.get(REQUESTS_SUBMITTED); }

    public void setRequestsSubmittedStat(Integer value) { this.statsValues.put(REQUESTS_SUBMITTED, value); }

    public Integer getPublicRequestsStat() { return this.statsValues.get(PUBLIC_REQUESTS); }

    public void setPublicRequestsStat(Integer value) { this.statsValues.put(PUBLIC_REQUESTS, value); }
}
