package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequestAnswer;

import java.time.LocalDateTime;

public class ClarificationRequestAnswerDto {
    private Integer creator;
    private Integer request;
    private String content;
    private LocalDateTime creationDate;

    public ClarificationRequestAnswerDto() {}

    public ClarificationRequestAnswerDto(ClarificationRequestAnswer answer) {
        this.request = answer.getRequest().getId();
        this.creator = answer.getCreator().getId();
        this.content = answer.getContent();

        if (answer.getCreationDate() != null)
            this.creationDate = answer.getCreationDate();
    }

    public Integer getCreator() { return creator; }
    public void setCreator(Integer id) { this.creator = id; }
    public Integer getRequestId() { return request; }
    public void setRequestId(Integer id) { this.request = id; }
    public String getContent() { return this.content; }
    public void setContent(String s) { this.content = s; }
    public void setCreationDate(LocalDateTime date) { this.creationDate = date; }
    public LocalDateTime getCreationDate() { return this.creationDate; }
}