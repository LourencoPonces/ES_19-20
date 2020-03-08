package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

@Service
public class TeacherEvaluatesStudentQuestionService {
    public void TeacherEvaluatesStudentQuestionService() {

    }

    public void acceptStudentQuestion(Integer studentQuestionKey) {

    }

    public void acceptStudentQuestion(Integer studentQuestionKey, String justification) {

    }

    public void rejectStudentQuestion(Integer studentQuestionKey, String justification) {
        // if justification is empty throw error
        // if already accepted throw error

    }
}