package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain;


import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "clarification_requests")

public class ClarificationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @ManyToOne                  //check this
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "submission_date")
    private LocalDateTime creationDate;

    public ClarificationRequest() {}

    public ClarificationRequest(User user, Question question, ClarificationRequestDto clarificationRequestDto) {
        checkConsistentClarificationRequest(user, question, clarificationRequestDto);
        this.owner = user;
        this.question = question;
        this.content = clarificationRequestDto.getContent();
    }

    private void checkConsistentClarificationRequest(User user, Question question, ClarificationRequestDto clarificationRequestDto) {
        // TODO throw necessary exceptions
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getOwner() { return owner; }
    public void setOwner(User student) { owner = student; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime date) { creationDate = date; }
}