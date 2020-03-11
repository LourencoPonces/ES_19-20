package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;

import java.util.List;
import java.util.ArrayList;

@Service
public class CheckStudentQuestionStatusService {

    public StudentQuestion.SubmittedStatus getStudentQuestionStatus(Integer studentQuestionId) {
        return StudentQuestion.SubmittedStatus.APPROVED;
    }

    public List<StudentQuestionDTO> getAllStudentQuestion(Integer studentId) {
        return new ArrayList<StudentQuestionDTO>();
    }

    public List<StudentQuestionDTO> getAllQuestionsWithStatus(Integer studentId, StudentQuestion.SubmittedStatus status) {
        return new ArrayList<StudentQuestionDTO>();
    }

}
