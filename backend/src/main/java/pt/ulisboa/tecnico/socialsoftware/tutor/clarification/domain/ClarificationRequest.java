package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain;


import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import java.time.LocalDateTime;
import javax.persistence.*;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CLARIFICATION_REQUEST_MISSING_CONTENT;

@Entity
@Table(name = "clarification_requests")

public class ClarificationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique=true, nullable = false)
    private Integer key;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "submission_date")
    private LocalDateTime creationDate;

    public ClarificationRequest() {}

    public ClarificationRequest(User user, Question question, ClarificationRequestDto clarificationRequestDto) {
        checkConsistentClarificationRequest(clarificationRequestDto);
        this.key = clarificationRequestDto.getKey();
        this.owner = user;
        this.question = question;
        this.content = clarificationRequestDto.getContent();
        this.creationDate = clarificationRequestDto.getCreationDateDate();
    }

    private void checkConsistentClarificationRequest(ClarificationRequestDto clarificationRequestDto) {
        if (clarificationRequestDto.getContent() == null || clarificationRequestDto.getContent().trim().length() == 0)
            throw new TutorException(CLARIFICATION_REQUEST_MISSING_CONTENT);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getKey() { return key; }
    public void setKey(Integer key) { this.key = key; }
    public User getOwner() { return owner; }
    public void setOwner(User student) { owner = student; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime date) { creationDate = date; }
}