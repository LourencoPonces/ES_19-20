package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain;


import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CLARIFICATION_REQUEST_MISSING_CONTENT;

@Entity
@Table(name = "clarification_request_answers")
public class ClarificationRequestAnswer {
    @Id
    private Integer id;

    @OneToOne
    @JoinColumn(name = "id")
    @MapsId
    private ClarificationRequest request;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User creator;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    private LocalDateTime submissionDate;

    public ClarificationRequestAnswer() {}

    public ClarificationRequestAnswer(ClarificationRequest req, User u, String answer) {
        this.creator = u;
        this.content = answer;
        this.submissionDate = LocalDateTime.now();
    }

    public ClarificationRequestAnswer(ClarificationRequest req, User creator, ClarificationRequestAnswerDto dto) {
        checkConsistentDto(dto);
        this.creator = creator;
        this.request = req;
        this.content = dto.getContent();
        this.submissionDate = dto.getCreationDate();
    }

    private void checkConsistentDto(ClarificationRequestAnswerDto dto) {
        if (dto.getContent() == null || dto.getContent().isBlank())
            throw new TutorException(CLARIFICATION_REQUEST_MISSING_CONTENT);
    }

    public User getCreator() { return this.creator; }
    public void setCreator(User u) { this.creator = u; }
    public ClarificationRequest getRequest() { return this.request; }
    public void setRequest(ClarificationRequest req) { this.request = req; }
    public String getContent() { return content; }
    public void setContent(String s) { this.content = s; }
    public LocalDateTime getCreationDate() { return submissionDate; }
    public void setCreationDate(LocalDateTime date) { this.submissionDate = date; }
}