package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Transient;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClarificationRequestDto {
    private Integer id;
    private Integer key;
    private Integer questionId;
    private ClarificationRequest.RequestStatus status = ClarificationRequest.RequestStatus.PRIVATE;
    private String creatorUsername;
    private String content;
    private Boolean resolved;
    private LocalDateTime creationDate;
    private ClarificationRequest.RequestType type;
    private final List<ClarificationMessageDto> messages;

    @Transient
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ClarificationRequestDto() {
        this.messages = new ArrayList<>();
    }

    public ClarificationRequestDto(ClarificationRequest clarificationRequest) {
        this.id = clarificationRequest.getId();
        this.questionId = clarificationRequest.getQuestion().getId();
        this.status = clarificationRequest.getStatus();
        this.creatorUsername = clarificationRequest.getCreator().getUsername();
        this.content = clarificationRequest.getContent();
        this.creationDate = clarificationRequest.getCreationDate();
        this.resolved = clarificationRequest.getResolved();
        this.type = clarificationRequest.getType();

        this.messages = clarificationRequest.getMessages()
                .stream()
                .map(ClarificationMessageDto::new)
                .collect(Collectors.toList());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getCreatorUsername() {
        return this.creatorUsername;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer id) {
        this.questionId = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<ClarificationMessageDto> getMessages() {
        return messages;
    }

    public ClarificationRequest.RequestStatus getStatus() {
        return this.status;
    }

    public void setStatus(ClarificationRequest.RequestStatus status) {
        this.status = status;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setType(ClarificationRequest.RequestType type) {
        this.type = type;
    }

    public ClarificationRequest.RequestType getType() {
        return type;
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
