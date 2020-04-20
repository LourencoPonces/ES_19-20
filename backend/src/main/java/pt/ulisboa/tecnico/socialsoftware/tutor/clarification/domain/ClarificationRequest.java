package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CLARIFICATION_REQUEST_MISSING_CONTENT;

@Entity
@Table(name = "clarification_requests")
public class ClarificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "request", fetch=FetchType.LAZY, orphanRemoval=true, optional=true)
    private ClarificationRequestAnswer answer;

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
    public Integer getKey() {
        if (this.key == null) {
            generateKeys();
        }
        return key;
    }

    private void generateKeys() {
        Integer max = this.question.getClarificationRequests().stream()
                .filter(request -> request.key != null)
                .map(ClarificationRequest::getKey)
                .max(Comparator.comparing(Integer::valueOf))
                .orElse(0);

        List<ClarificationRequest> nullKeyClarificationRequests = this.question.getClarificationRequests().stream()
                .filter(cr -> cr.key == null).collect(Collectors.toList());

        for (ClarificationRequest cr : nullKeyClarificationRequests) {
            max = max + 1;
            cr.key = max;

        }
    }

    public void setKey(Integer key) { this.key = key; }
    public User getOwner() { return owner; }
    public void setOwner(User student) { owner = student; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime date) { creationDate = date; }
    public Optional<ClarificationRequestAnswer> getAnswer() { return Optional.ofNullable(answer); }
    public void setAnswer(ClarificationRequestAnswer a) { this.answer = a; }
    public void removeAnswer() { this.setAnswer(null); }
    public boolean hasAnswer() {return this.answer != null; }
}