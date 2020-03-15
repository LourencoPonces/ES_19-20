package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;


import javax.persistence.*;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@Table(
        name = "student_question",
        indexes = {
                @Index(name = "question_indx_0", columnList = "student_question_key")
        })
public class StudentQuestion extends Question {

    public enum SubmittedStatus {
        WAITING_FOR_APPROVAL, REJECTED, APPROVED
    }

    // Do we need to have an id and key column?
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique=true, nullable = false, name="student_question_key")
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

        for (TopicDto topicDto : questionDto.getTopics()) {
            Topic t = new Topic(course, topicDto);
            addTopic(t);
        }

    }

    @Override
    public Integer getId() { return id; }

    @Override
    public void setId(Integer id) { this.id = id; }

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

        if(user.getRole() != User.Role.STUDENT) {
            throw new TutorException(ACCESS_DENIED);
        }
    }

    @Override
    public String toString() {
        return "StudentQuestion{" +
                "id=" + id +
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
