package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MyStatsDto implements Serializable {
    private Map<String, Integer> statsValues = new HashMap<>();
    private Map<String, MyStats.StatsVisibility> statsVisibility = new HashMap<>();

    private final String REQUESTS_SUBMITTED = "requestsSubmitted";
    private final String PUBLIC_REQUESTS = "publicRequests";
    private final String SUBMITTED_QUESTIONS = "submittedQuestions";
    private final String APPROVED_QUESTIONS = "acceptedQuestions";

    public MyStatsDto() { }

    public MyStatsDto(MyStats myStats) {
        this.statsVisibility.put(REQUESTS_SUBMITTED, myStats.getRequestsSubmitted());
        this.statsVisibility.put(PUBLIC_REQUESTS, myStats.getPublicRequests());
        this.statsVisibility.put(SUBMITTED_QUESTIONS, myStats.getSubmittedQuestions());
        this.statsVisibility.put(APPROVED_QUESTIONS, myStats.getApprovedQuestions());
    }

    // ======================================
    // DDP
    // ======================================

    public MyStats.StatsVisibility getRequestsSubmittedVisibility() {
        return this.statsVisibility.get(REQUESTS_SUBMITTED);
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

    // ======================================
    // PPA
    // ======================================

    public MyStats.StatsVisibility getSubmittedQuestionsVisibility() {
        return this.statsVisibility.get(SUBMITTED_QUESTIONS);
    }

    public void setSubmittedQuestionsVisibility(MyStats.StatsVisibility visibility) {
        this.statsVisibility.put(SUBMITTED_QUESTIONS, visibility);
    }

    public MyStats.StatsVisibility getApprovedQuestionsVisibility() {
        return this.statsVisibility.get(APPROVED_QUESTIONS);
    }

    public void setApprovedQuestionsVisibility(MyStats.StatsVisibility visibility) {
        this.statsVisibility.put(APPROVED_QUESTIONS, visibility);
    }

    public Integer getSubmittedQuestionsStat() { return this.statsValues.get(SUBMITTED_QUESTIONS); }
    public void setSubmittedQuestionsStat(Integer value) { this.statsValues.put(SUBMITTED_QUESTIONS, value); }

    public Integer getApprovedQuestionsStat() { return this.statsValues.get(APPROVED_QUESTIONS); }
    public void setApprovedQuestionsStat(Integer value) { this.statsValues.put(APPROVED_QUESTIONS, value); }

}
