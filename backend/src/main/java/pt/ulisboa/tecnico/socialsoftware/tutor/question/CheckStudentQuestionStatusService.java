package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

@Service
public class CheckStudentQuestionStatusService {

    public StudentQuestion.SubmittedStatus getStudentQuestionStatus(Integer studentQuestionId) {
        return StudentQuestion.SubmittedStatus.APPROVED;
    }

}
