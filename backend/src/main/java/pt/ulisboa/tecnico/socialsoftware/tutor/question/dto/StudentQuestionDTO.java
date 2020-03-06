package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

public class StudentQuestionDTO extends QuestionDto{
    private String justification;
    private String username;


    public StudentQuestionDTO() {}

    public StudentQuestionDTO(StudentQuestion studentQuestion) {
        super(studentQuestion);
        justification = studentQuestion.getJustification();
        username = studentQuestion.getUser().getUsername();
    }

    public String getJustification() { return justification; }
    public void setJustification(String justification) {this.justification = justification; }

    public String getUser() { return username; }
    public void setUser(String username) { this.username = username; }

    @Override
    public String toString() {
        return "QuestionDto{" +
                "id=" + this.getId() +
                ", id=" + this.getId() +
                ", title='" + this.getTitle() + '\'' +
                ", content='" + this.getContent() + '\'' +
                ", difficulty=" + this.getDifficulty() +
                ", numberOfAnswers=" + this.getNumberOfAnswers() +
                ", numberOfCorrect=" + this.getNumberOfCorrect() +
                ", status='" + this.getStatus() + '\'' +
                ", options=" + this.getOptions() +
                ", image=" + this.getImage() +
                ", topics=" + this.getTopics() +
                ", sequence=" + this.getSequence() +
                ", user_id=" + this.getUser() +
                ", justification=" + this.getJustification() +
                '}';
    }
}
