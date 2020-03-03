package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto;

import org.springframework.data.annotation.Transient;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest;
import java.time.format.DateTimeFormatter;

public class ClarificationRequestDto {
    private Integer id;
    private String owner;
    private Integer question;
    private String content;
    private String creationDate = null;

    @Transient
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ClarificationRequestDto() {}

    public ClarificationRequestDto(ClarificationRequest clarificationRequest) {
        this.id = clarificationRequest.getId();
        this.content = clarificationRequest.getContent();
        this.question = clarificationRequest.getQuestion().getId();
        this.owner = clarificationRequest.getOwner().getUsername();

        if (clarificationRequest.getCreationDate() != null)
            this.creationDate = clarificationRequest.getCreationDate().format(formatter);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getOwner() { return owner; }
    public void setOwner(String username) { this.owner = username; }
    public Integer getQuestionId() { return question; }
    public void setQuestionId(Integer id) { this.question = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCreationDate() { return creationDate; }
    public void setCreationDate(String date) { creationDate = date; }
}