package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequestAnswer;

import java.time.Instant;

public class ClarificationRequestAnswerDto {
    private Integer creator;
    private Integer request;
    private String content;
    private Instant creationDate;

    public ClarificationRequestAnswerDto() {}

    public ClarificationRequestAnswerDto(ClarificationRequestAnswer clarificationRequest) {
        this.request = clarificationRequest.getRequest().getId();
        this.creator = clarificationRequest.getCreator().getId();
        this.content = clarificationRequest.getContent();

        if (clarificationRequest.getCreationDate() != null)
            this.creationDate = clarificationRequest.getCreationDate();
    }

    public Integer getCreator() { return creator; }
    public void setCreator(Integer id) { this.creator = id; }
    public Integer getRequestId() { return request; }
    public void setRequestId(Integer id) { this.request = id; }
    public String getContent() { return this.content; }
    public void setContent(String s) { this.content = s; }
    public void setCreationDate(Instant date) { this.creationDate = date; }
    public Instant getCreationDate() { return this.creationDate; }
}