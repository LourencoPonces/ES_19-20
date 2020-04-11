package pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class StatementQuestionDto implements Serializable {
    private String content;
    private List<StatementOptionDto> options;
    private ImageDto image;
    private Integer sequence;
    private Integer questionId;
    private List<ClarificationRequestDto> clarifications = new ArrayList<>();

    public StatementQuestionDto(QuestionAnswer questionAnswer) {
        this.questionId = questionAnswer.getQuizQuestion().getQuestion().getId();
        this.content = questionAnswer.getQuizQuestion().getQuestion().getContent();
        if (questionAnswer.getQuizQuestion().getQuestion().getImage() != null) {
            this.image = new ImageDto(questionAnswer.getQuizQuestion().getQuestion().getImage());
        }
        this.options = questionAnswer.getQuizQuestion().getQuestion().getOptions().stream().map(StatementOptionDto::new).collect(Collectors.toList());
        this.sequence = questionAnswer.getSequence();
        this.clarifications = questionAnswer.getQuizQuestion().getQuestion().getClarificationRequests().stream().map(ClarificationRequestDto::new).collect(Collectors.toList());
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<StatementOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<StatementOptionDto> options) {
        this.options = options;
    }

    public ImageDto getImage() {
        return image;
    }

    public void setImage(ImageDto image) {
        this.image = image;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Integer getQuestionId() { return questionId; }

    public void setQuestionId(Integer questionId) { this.questionId = questionId; }

    public List<ClarificationRequestDto> getClarifications() { return clarifications; }

    public void setClarifications(List<ClarificationRequestDto> clarifications) { this.clarifications = clarifications; }

    @Override
    public String toString() {
        return "StatementQuestionDto{" +
                ", content='" + content + '\'' +
                ", options=" + options +
                ", image=" + image +
                ", sequence=" + sequence +
                '}';
    }
}