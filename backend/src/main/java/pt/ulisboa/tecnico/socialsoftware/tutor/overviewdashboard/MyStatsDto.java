package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MyStatsDto implements Serializable {

    private Integer id;
    private Map<String, Integer> statsValues = new HashMap<>();
    private Map<String, MyStats.StatsVisibility> statsVisibility = new HashMap<>();

    private static final String SUBMITTED_QUESTIONS = "submittedQuestions";
    private static final String APPROVED_QUESTIONS = "approvedQuestions";
    private static final String REQUESTS_SUBMITTED = "requestsSubmitted";
    private static final String PUBLIC_REQUESTS = "publicRequests";
    private static final String TOURNAMENTS_PARTICIPATED = "tournamentsParticipated";
    private static final String TOURNAMENTS_SCORE = "tournamentsScore";

    public MyStatsDto() { }

    public MyStatsDto(MyStats myStats) {
        this.id = myStats.getId();
        this.statsVisibility.put(REQUESTS_SUBMITTED, myStats.getRequestsSubmittedVisibility());
        this.statsVisibility.put(PUBLIC_REQUESTS, myStats.getPublicRequestsVisibility());
        this.statsVisibility.put(SUBMITTED_QUESTIONS, myStats.getSubmittedQuestionsVisibility());
        this.statsVisibility.put(APPROVED_QUESTIONS, myStats.getApprovedQuestionsVisibility());
        this.statsVisibility.put(TOURNAMENTS_PARTICIPATED, myStats.getTournamentsParticipated());
        this.statsVisibility.put(TOURNAMENTS_SCORE, myStats.getTournamentsScore());
    }

    public MyStatsDto(MyStats myStats, MyStatsDto oldStatsDto) {
        this(myStats);
        setApprovedQuestionsStat(oldStatsDto.getApprovedQuestionsStat());
        setSubmittedQuestionsStat(oldStatsDto.getSubmittedQuestionsStat());
        setRequestsSubmittedStat(oldStatsDto.getRequestsSubmittedStat());
        setPublicRequestsStat(oldStatsDto.getPublicRequestsStat());
        setTournamentsParticipatedStat(oldStatsDto.getTournamentsParticipatedStat());
        setTournamentsScoreStat(oldStatsDto.getTournamentsScoreStat());
    }

    public Integer getId() {
        return id;
    }

    // ======================================
    // DDP
    // ======================================

    @JsonIgnore
    public MyStats.StatsVisibility getRequestsSubmittedVisibility() { return this.statsVisibility.get(REQUESTS_SUBMITTED); }

    public void setRequestsSubmittedVisibility(MyStats.StatsVisibility visibility) { this.statsVisibility.put(REQUESTS_SUBMITTED, visibility); }

    @JsonIgnore
    public MyStats.StatsVisibility getPublicRequestsVisibility() {
        return this.statsVisibility.get(PUBLIC_REQUESTS);
    }

    public void setPublicRequestsVisibility(MyStats.StatsVisibility visibility) { this.statsVisibility.put(PUBLIC_REQUESTS, visibility); }

    @JsonIgnore
    public Integer getRequestsSubmittedStat() { return this.statsValues.get(REQUESTS_SUBMITTED); }

    public void setRequestsSubmittedStat(Integer value) { this.statsValues.put(REQUESTS_SUBMITTED, value); }

    @JsonIgnore
    public Integer getPublicRequestsStat() { return this.statsValues.get(PUBLIC_REQUESTS); }

    public void setPublicRequestsStat(Integer value) { this.statsValues.put(PUBLIC_REQUESTS, value); }

    // ======================================
    // PPA
    // ======================================

    @JsonIgnore
    public MyStats.StatsVisibility getSubmittedQuestionsVisibility() { return this.statsVisibility.get(SUBMITTED_QUESTIONS); }

    public void setSubmittedQuestionsVisibility(MyStats.StatsVisibility visibility) { this.statsVisibility.put(SUBMITTED_QUESTIONS, visibility); }

    @JsonIgnore
    public MyStats.StatsVisibility getApprovedQuestionsVisibility() { return this.statsVisibility.get(APPROVED_QUESTIONS); }

    public void setApprovedQuestionsVisibility(MyStats.StatsVisibility visibility) { this.statsVisibility.put(APPROVED_QUESTIONS, visibility); }

    @JsonIgnore
    public Integer getSubmittedQuestionsStat() { return this.statsValues.get(SUBMITTED_QUESTIONS); }

    public void setSubmittedQuestionsStat(Integer value) { this.statsValues.put(SUBMITTED_QUESTIONS, value); }

    @JsonIgnore
    public Integer getApprovedQuestionsStat() { return this.statsValues.get(APPROVED_QUESTIONS); }

    public void setApprovedQuestionsStat(Integer value) { this.statsValues.put(APPROVED_QUESTIONS, value); }

    // ======================================
    // TDP
    // ======================================

    @JsonIgnore
    public MyStats.StatsVisibility getTournamentsParticipatedVisibility() { return this.statsVisibility.get(TOURNAMENTS_PARTICIPATED); }

    public void setTournamentsParticipatedVisibility(MyStats.StatsVisibility visibility) { this.statsVisibility.put(TOURNAMENTS_PARTICIPATED, visibility); }

    @JsonIgnore
    public MyStats.StatsVisibility getTournamentsScoreVisibility() { return this.statsVisibility.get(TOURNAMENTS_SCORE); }

    public void setTournamentsScoreVisibility(MyStats.StatsVisibility visibility) { this.statsVisibility.put(TOURNAMENTS_SCORE, visibility); }

    @JsonIgnore
    public Integer getTournamentsParticipatedStat() { return this.statsValues.get(TOURNAMENTS_PARTICIPATED); }

    public void setTournamentsParticipatedStat(Integer value) { this.statsValues.put(TOURNAMENTS_PARTICIPATED, value); }

    @JsonIgnore
    public Integer getTournamentsScoreStat() { return this.statsValues.get(TOURNAMENTS_SCORE); }

    public void setTournamentsScoreStat(Integer value) { this.statsValues.put(TOURNAMENTS_SCORE, value); }

    public Map<String, Integer> getStatsValues() {
        return statsValues;
    }

    public void setStatsValues(Map<String, Integer> statsValues) {
        this.statsValues = statsValues;
    }

    public Map<String, MyStats.StatsVisibility> getStatsVisibility() {
        return statsVisibility;
    }

    public void setStatsVisibility(Map<String, MyStats.StatsVisibility> statsVisibility) { this.statsVisibility = statsVisibility; }
}
