package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;


import javax.persistence.*;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;


@Entity
public class StudentQuestion extends Question {

    public enum SubmittedStatus {
        WAITING_FOR_APPROVAL, REJECTED, APPROVED
    }

    // Do we need to have an id and key column?

    @Column
    private String justification = "";

    @Enumerated(EnumType.STRING)
    private SubmittedStatus submittedStatus = SubmittedStatus.WAITING_FOR_APPROVAL;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;



    public StudentQuestion() {}

    public StudentQuestion(Course course, QuestionDto questionDto, User student) {
        super(course, questionDto);
        user = student;
    }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public SubmittedStatus getSubmittedStatus() { return submittedStatus; }
    public void setSubmittedStatus(SubmittedStatus status) { this.submittedStatus = status; }
}
