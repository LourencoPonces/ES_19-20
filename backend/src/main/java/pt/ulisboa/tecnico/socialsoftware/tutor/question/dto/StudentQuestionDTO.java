package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

public class StudentQuestionDTO extends QuestionDto{
    private Integer id;
    private Integer studentQuestionKey;
    private String justification;
    private String acceptArg;
    private StudentQuestion.SubmittedStatus submittedStatus;
    private String username;


    public StudentQuestionDTO() {}

    public StudentQuestionDTO(StudentQuestion studentQuestion) {
        super(studentQuestion);
        this.id = studentQuestion.getId();
        this.studentQuestionKey = studentQuestion.getStudentQuestionKey();
        justification = studentQuestion.getJustification();
        acceptArg = studentQuestion.getAcceptArg();
        submittedStatus = studentQuestion.getSubmittedStatus();
        username = studentQuestion.getUser().getUsername();
    }

    @Override
    public Integer getId() { return id; }

    public void setId(Integer id) { this.id = id; }


    public Integer getStudentQuestionKey() {
        return studentQuestionKey;
    }

    public void setStudentQuestionKey(Integer studentQuestionKey) {
        this.studentQuestionKey = studentQuestionKey;
    }

    public String getJustification() { return justification; }
    public void setJustification(String justification) {this.justification = justification; }

    public String getAcceptArg() {
        return acceptArg;
    }

    public void setAcceptArg(String acceptArg) {
        this.acceptArg = acceptArg;
    }

    public String getUser() { return username; }
    public void setUser(String username) { this.username = username; }

    public StudentQuestion.SubmittedStatus getSubmittedStatus() { return submittedStatus; }
    public void setSubmittedStatus(StudentQuestion.SubmittedStatus submittedStatus) { this.submittedStatus = submittedStatus; }

    @Override
    public String toString() {
        return "StudentQuestionDto{" +
                "id=" + this.getId() +
                ", key=" + this.getKey() +
                ", studentQuestionKey=" + this.getStudentQuestionKey() +
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
                ", accept argument=" + this.getAcceptArg() +
                '}';
    }
}
