package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "clarification_requests")
public class ClarificationRequest {

    public enum RequestStatus {
        PRIVATE, PUBLIC
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer key;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    private LocalDateTime creationDate = LocalDateTime.now();

    private String content;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "request", orphanRemoval = true)
    @OrderBy("creationDate ASC")
    private List<ClarificationMessage> messages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PRIVATE;

    private Boolean resolved = false;

    public ClarificationRequest() {
    }

    public ClarificationRequest(Question question, User creator, ClarificationRequestDto clarificationRequestDto) {
        this.key = clarificationRequestDto.getKey();
        this.question = question;
        this.status = clarificationRequestDto.getStatus();
        this.creator = creator;
        this.content = clarificationRequestDto.getContent();

        this.ensureConsistent();
    }

    private void ensureConsistent() {
        if (this.content == null || this.content.isBlank())
            throw new TutorException(ErrorMessage.CLARIFICATION_REQUEST_MISSING_CONTENT);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getKey() {
        if (this.key == null) {
            generateKeys();
        }
        return key;
    }

    private void generateKeys() {
        int max = this.question.getClarificationRequests().stream()
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

    public void setKey(Integer key) {
        this.key = key;
    }

    public User getCreator() {
        return this.creator;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public LocalDateTime getCreationDate() {
        return this.creationDate;
    }

    public String getContent() {
        return content;
    }

    public RequestStatus getStatus() {
        return this.status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Boolean getResolved() {
        return this.resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public List<ClarificationMessage> getMessages() {
        return messages;
    }
}