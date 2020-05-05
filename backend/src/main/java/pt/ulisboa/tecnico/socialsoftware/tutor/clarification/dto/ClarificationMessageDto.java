package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Transient;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClarificationMessageDto {
    private Integer id;
    private Integer creatorId;
    private Integer requestId;
    private String content;
    private LocalDateTime creationDate;

    // to set request resolved property when submitting a reply
    private Boolean resolved;

    @Transient
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ClarificationMessageDto() {}

    public ClarificationMessageDto(ClarificationMessage answer) {
        this.id = answer.getId();
        this.requestId = answer.getRequest().getId();
        this.creatorId = answer.getCreator().getId();
        this.content = answer.getContent();

        if (answer.getCreationDate() != null)
            this.creationDate = answer.getCreationDate();
    }

    public Integer getId() {
        return this.id;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer id) {
        this.creatorId = id;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer id) {
        this.requestId = id;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String s) {
        this.content = s;
    }

    public Boolean getResolved() {
        return this.resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    @JsonProperty
    public String getCreationDate() {
        return this.creationDate.format(this.formatter);
    }

    @JsonProperty
    public void setCreationDate(String s) {
        this.creationDate = LocalDateTime.parse(s, this.formatter);
    }


    @JsonIgnore
    public void setCreationDateDate(LocalDateTime date) {
        this.creationDate = date;
    }

    @JsonIgnore
    public LocalDateTime getCreationDateDate() {
        return this.creationDate;
    }
}