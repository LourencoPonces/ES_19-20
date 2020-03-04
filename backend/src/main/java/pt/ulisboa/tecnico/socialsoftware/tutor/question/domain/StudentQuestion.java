package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;


import javax.persistence.*;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;



    public StudentQuestion() {}

    public StudentQuestion(Course course, StudentQuestionDTO questionDto, User student) {
        super(course, questionDto);
        checkStudentQuestionConsistency(questionDto);
        user = student;
    }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public SubmittedStatus getSubmittedStatus() { return submittedStatus; }
    public void setSubmittedStatus(SubmittedStatus status) { this.submittedStatus = status; }

    public void checkStudentQuestionConsistency(StudentQuestionDTO questionDto) {

        if ((long) questionDto.getTopics().size() == 0) {
            throw new TutorException(NO_TOPICS);
        }
    }
}
