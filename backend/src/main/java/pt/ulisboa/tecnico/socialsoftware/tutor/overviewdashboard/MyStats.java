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

    @Column(name="requests_submitted", nullable = false)
    private StatsVisibility requestsSubmitted;

    @Column(name="public_requests", nullable = false)
    private StatsVisibility publicRequests;

    @Column(name="submitted_questions", nullable = false)
    private StatsVisibility submittedQuestions;

    @Column(name="approved_questions", nullable = false)
    private StatsVisibility approvedQuestions;

    @Column(name="tournaments_participated", nullable = false)
    private StatsVisibility tournamentsParticipated;

    @Column(name="tournaments_score", nullable = false)
    private StatsVisibility tournamentsScore;

    public MyStats() {}

    public MyStats(User user, StatsVisibility statsVisibility) {
        this.user = user;
        this.requestsSubmitted = statsVisibility;
        this.publicRequests = statsVisibility;
        this.submittedQuestions = statsVisibility;
        this.approvedQuestions = statsVisibility;
        this.tournamentsParticipated = statsVisibility;
        this.tournamentsScore = statsVisibility;
    }

    public Integer getId() {
        return id;
    }

    public User getUser() { return this.user; }

    public void setUser(User user) { this.user = user; }

    // Number of clarification requests submitted
    public StatsVisibility getRequestsSubmittedVisibility() {
        return this.requestsSubmitted;
    }

    public void setRequestsSubmittedVisibility(StatsVisibility requestsSubmitted) { this.requestsSubmitted = requestsSubmitted; }

    public boolean canSeeRequestsSubmitted() {
        return this.requestsSubmitted == StatsVisibility.PUBLIC;
    }

    // Number of submitted clarification requests that were made public
    public StatsVisibility getPublicRequestsVisibility() { return this.publicRequests; }
    
    public void setPublicRequestsVisibility(StatsVisibility publicRequests) {
        this.publicRequests = publicRequests;
    }

    public boolean canSeePublicRequests() {
        return this.publicRequests == StatsVisibility.PUBLIC;
    }

    public StatsVisibility getSubmittedQuestionsVisibility() {
        return submittedQuestions;
    }

    public void setSubmittedQuestionsVisibility(StatsVisibility proposedQuestions) { this.submittedQuestions = proposedQuestions; }

    public boolean canSeeSubmittedQuestions() {
        return this.submittedQuestions == StatsVisibility.PUBLIC;
    }

    public StatsVisibility getApprovedQuestionsVisibility() {
        return approvedQuestions;
    }

    public void setApprovedQuestionsVisibility(StatsVisibility approvedQuestions) { this.approvedQuestions = approvedQuestions; }

    public boolean canSeeApprovedQuestions() {
        return this.approvedQuestions == StatsVisibility.PUBLIC;
    }

    public StatsVisibility getTournamentsParticipated() { return this.tournamentsParticipated; }

    public void setTournamentsParticipated(StatsVisibility tournamentsParticipated) { this.tournamentsParticipated = tournamentsParticipated; }

    public boolean canSeeTournamentsParticipated() { return this.tournamentsParticipated == StatsVisibility.PUBLIC; }

    public StatsVisibility getTournamentsScore() { return this.tournamentsScore; }

    public void setTournamentsScore(StatsVisibility tournamentsScore) { this.tournamentsScore = tournamentsScore; }

    public boolean canSeeTournamentsScore() { return this.tournamentsScore == StatsVisibility.PUBLIC; }


    public void updateVisibility(MyStatsDto myStatsDto) {
        setApprovedQuestionsVisibility(myStatsDto.getApprovedQuestionsVisibility());
        setSubmittedQuestionsVisibility(myStatsDto.getSubmittedQuestionsVisibility());
        setPublicRequestsVisibility(myStatsDto.getPublicRequestsVisibility());
        setRequestsSubmittedVisibility(myStatsDto.getRequestsSubmittedVisibility());
        setTournamentsParticipated(myStatsDto.getTournamentsParticipatedVisibility());
        setTournamentsScore(myStatsDto.getTournamentsScoreVisibility());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitMyStats(this);
    }

}
