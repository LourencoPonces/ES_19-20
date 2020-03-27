package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequestAnswer;

import java.time.LocalDateTime;

public class ClarificationRequestAnswerDto {
    private Integer creatorId;
    private Integer requestId;
    private String content;
    private LocalDateTime creationDate;

    public ClarificationRequestAnswerDto(ClarificationRequestAnswer answer) {
        this.requestId = answer.getRequest().getId();
        this.creatorId = answer.getCreator().getId();
        this.content = answer.getContent();

        if (answer.getCreationDate() != null)
            this.creationDate = answer.getCreationDate();
    }

    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer id) { this.creatorId = id; }
    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer id) { this.requestId = id; }
    public String getContent() { return this.content; }
    public void setContent(String s) { this.content = s; }
    public void setCreationDate(LocalDateTime date) { this.creationDate = date; }
    public LocalDateTime getCreationDate() { return this.creationDate; }
}