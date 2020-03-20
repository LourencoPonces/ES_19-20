package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;


public class EvaluationDto {
    private StudentQuestion.SubmittedStatus evaluation;
    private String justification = "";

    public EvaluationDto(StudentQuestion.SubmittedStatus s, String j) {
        evaluation = s;
        justification = j;
    }

    public EvaluationDto () {}

    public StudentQuestion.SubmittedStatus getEvaluation() {
        return evaluation;
    }

    public String getJustification() {
        return justification;
    }
}
