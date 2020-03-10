package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;

import java.util.Set;
import java.util.TreeSet;

@Service
public class CheckStudentQuestionStatusService {

    public StudentQuestion.SubmittedStatus getStudentQuestionStatus(Integer studentQuestionId) {
        return StudentQuestion.SubmittedStatus.APPROVED;
    }

    public Set<StudentQuestionDTO> getAllStudentQuestion(Integer studentId) {
        return new TreeSet<StudentQuestionDTO>();
    }

    public Set<StudentQuestionDTO> getAllApprovedStudentQuestions(Integer studentId) {
        return new TreeSet<StudentQuestionDTO>();
    }

    public Set<StudentQuestionDTO> getAllRejectedStudentQuestions(Integer studentId) {
        return new TreeSet<StudentQuestionDTO>();
    }

    public Set<StudentQuestionDTO> getAllPendingStudentQuestions(Integer studentId) {
        return new TreeSet<StudentQuestionDTO>();
    }

}
