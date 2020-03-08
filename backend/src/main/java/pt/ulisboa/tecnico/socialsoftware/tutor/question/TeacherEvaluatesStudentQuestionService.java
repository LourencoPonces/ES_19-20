package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository;

import java.sql.SQLException;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class TeacherEvaluatesStudentQuestionService {

    @Autowired
    private StudentQuestionRepository studentQuestionRepository;


    public void TeacherEvaluatesStudentQuestionService() {

    }

    public void acceptStudentQuestion(Integer studentQuestionId) {
        // accept with empty justification
        acceptStudentQuestion(studentQuestionId, "");
    }

    public void acceptStudentQuestion(Integer studentQuestionId, String justification) {
        StudentQuestion studentQuestion = findStudentQuestionByKey(studentQuestionId);

        // set to approved
        studentQuestion.setSubmittedStatus(StudentQuestion.SubmittedStatus.APPROVED);

        // set justification
        studentQuestion.setJustification(justification);
    }

    public void rejectStudentQuestion(Integer studentQuestionId, String justification) {
        // if invalid justification (empty)
        if(justification.isBlank()) {
            throw new TutorException(INVALID_JUSTIFICATION, justification);
        }


        StudentQuestion studentQuestion = findStudentQuestionByKey(studentQuestionId);

        // if already accepted throw error
        // change later. the question may exist in quizzes, etc
        if(studentQuestion.getSubmittedStatus() == StudentQuestion.SubmittedStatus.APPROVED) {
            throw new TutorException(CANNOT_REJECT_ACCEPTED_SUGGESTION);
        }

        studentQuestion.setSubmittedStatus(StudentQuestion.SubmittedStatus.REJECTED);
        studentQuestion.setJustification(justification);
    }


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    private StudentQuestion findStudentQuestionByKey(Integer id) {
         return studentQuestionRepository.findById(id).orElseThrow(() -> new TutorException(STUDENT_QUESTION_NOT_FOUND, id));
    }
}
