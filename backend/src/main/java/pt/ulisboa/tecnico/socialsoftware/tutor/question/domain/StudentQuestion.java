package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;


import javax.persistence.*;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@Table(
        name = "student_question",
        uniqueConstraints=@UniqueConstraint(columnNames = {"student_question_key", "user_id"}),
        indexes = {
                @Index(name = "student_question_indx_0", columnList="student_question_key, user_id")
        })
public class StudentQuestion extends Question {

    public enum SubmittedStatus {
        WAITING_FOR_APPROVAL, REJECTED, APPROVED
    }



    @Column(name="student_question_key")
    private Integer studentQuestionKey;


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
        checkStudentQuestionConsistency(questionDto, student);
        this.user = student;
        this.studentQuestionKey = questionDto.getStudentQuestionKey();
        if(questionDto.getSubmittedStatus() != null) {
            submittedStatus = questionDto.getSubmittedStatus();
        }
        justification = questionDto.getJustification();

    }

    public Integer getStudentQuestionKey() { return studentQuestionKey; }

    public void setStudentQuestionKey(Integer studentQuestionKey) { this.studentQuestionKey = studentQuestionKey; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public SubmittedStatus getSubmittedStatus() { return submittedStatus; }
    public void setSubmittedStatus(SubmittedStatus status) { this.submittedStatus = status; }

    public void checkStudentQuestionConsistency(StudentQuestionDTO questionDto, User user) {

        if ((long) questionDto.getTopics().size() == 0) {
            throw new TutorException(NO_TOPICS);
        }

        if(user.getRole() != User.Role.STUDENT || !user.getUsername().equals(questionDto.getUser())) {
            throw new TutorException(ACCESS_DENIED);
        }

        if(questionDto.getSubmittedStatus() != getSubmittedStatus()) {
            throw new TutorException(DIFFERENT_STATUS);
        }
    }

    public void update(StudentQuestionDTO studentQuestionDTO) {
        checkStudentQuestionConsistency(studentQuestionDTO, user);
        super.update(studentQuestionDTO);
        setJustification(studentQuestionDTO.getJustification());
        setStudentQuestionKey(studentQuestionDTO.getStudentQuestionKey());

    }

    @Override
    public void remove() {
        canRemoveStudentQuestion();
        getUser().getStudentQuestions().remove(this);
        super.remove();
    }

    public void canRemoveStudentQuestion() {
        if(getSubmittedStatus() != SubmittedStatus.WAITING_FOR_APPROVAL) {
            throw new TutorException(QUESTION_ALREADY_READ, getId());
        }
    }

    @Override
    public String toString() {
        return "StudentQuestion{" +
                "id=" + getId() +
                ", key=" + getKey() +
                ", studentQuestionKey=" + studentQuestionKey +
                ", content='" + getContent() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", numberOfAnswers=" + getNumberOfAnswers() +
                ", numberOfCorrect=" + getNumberOfCorrect() +
                ", status=" + getStatus() +
                ", image=" + getImage() +
                ", options=" + getOptions() +
                ", topics=" + getTopics() +
                '}';
    }
}
