package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain;


import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationMessageDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CLARIFICATION_MESSAGE_MISSING_CONTENT;

@Entity
@Table(name = "clarification_messages")
public class ClarificationMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ClarificationRequest request;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private User creator;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate = LocalDateTime.now();

    public ClarificationMessage() {
    }

    public ClarificationMessage(ClarificationRequest req, User creator, ClarificationMessageDto dto) {
        this.creator = creator;
        this.request = req;
        this.content = dto.getContent();

        this.ensureConsistent();
    }

    private void ensureConsistent() {
        if (this.content == null || this.content.isBlank())
            throw new TutorException(CLARIFICATION_MESSAGE_MISSING_CONTENT);
    }

    public Integer getId() {
        return this.id;
    }

    public User getCreator() {
        return this.creator;
    }

    public void setCreator(User u) {
        this.creator = u;
    }

    public ClarificationRequest getRequest() {
        return this.request;
    }

    public void setRequest(ClarificationRequest req) {
        this.request = req;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String s) {
        this.content = s;
        this.ensureConsistent();
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime date) {
        this.creationDate = date;
    }
}