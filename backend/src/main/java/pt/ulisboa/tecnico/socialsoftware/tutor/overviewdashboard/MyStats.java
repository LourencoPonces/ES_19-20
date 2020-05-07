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

    public MyStats() {}

    public MyStats(User user, StatsVisibility statsVisibility) {
        this.user = user;
        this.requestsSubmitted = statsVisibility;
        this.publicRequests = statsVisibility;
        this.submittedQuestions = statsVisibility;
        this.approvedQuestions = statsVisibility;
    }

    public User getUser() { return this.user; }
    public void setUser(User user) { this.user = user; }

    // Number of clarification requests submitted
    public StatsVisibility getRequestsSubmitted() {
        return this.requestsSubmitted;
    }

    public void setRequestsSubmitted(StatsVisibility requestsSubmitted) {
        this.requestsSubmitted = requestsSubmitted;
    }
    public boolean canSeeRequestsSubmitted() {
        return this.requestsSubmitted == StatsVisibility.PUBLIC;
    }

    // Number of submitted clarification requests that were made public
    public StatsVisibility getPublicRequests() { return this.publicRequests; }
    
    public void setPublicRequests(StatsVisibility publicRequests) {
        this.publicRequests = publicRequests;
    }
    public boolean canSeePublicRequests() {
        return this.publicRequests == StatsVisibility.PUBLIC;
    }

    public StatsVisibility getSubmittedQuestions() {
        return submittedQuestions;
    }

    public void setSubmittedQuestions(StatsVisibility proposedQuestions) {
        this.submittedQuestions = proposedQuestions;
    }

    public boolean canSeeSubmittedQuestions() {
        return this.submittedQuestions == StatsVisibility.PUBLIC;
    }

    public StatsVisibility getApprovedQuestions() {
        return approvedQuestions;
    }

    public void setApprovedQuestions(StatsVisibility approvedQuestions) {
        this.approvedQuestions = approvedQuestions;
    }

    public boolean canSeeApprovedQuestions() {
        return this.approvedQuestions == StatsVisibility.PUBLIC;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitMyStats(this);
    }

}
