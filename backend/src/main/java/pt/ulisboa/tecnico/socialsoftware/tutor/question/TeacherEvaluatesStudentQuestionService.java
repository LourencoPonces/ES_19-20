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
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class TeacherEvaluatesStudentQuestionService {

    @Autowired
    private StudentQuestionRepository studentQuestionRepository;


    public void TeacherEvaluatesStudentQuestionService() {}


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<StudentQuestionDTO> getAllStudentQuestions(int courseId) {
        return studentQuestionRepository.findByCourse(courseId).stream().map(StudentQuestionDTO::new).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<StudentQuestionDTO> getAllStudentQuestionsWithStatus(int courseId, StudentQuestion.SubmittedStatus status) {
        return studentQuestionRepository.findByCourseAndStatus(courseId, status.toString()).stream().map(StudentQuestionDTO::new).collect(Collectors.toList());
    }


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDTO acceptStudentQuestion(Integer studentQuestionId, String justification) {
        if(justification == null) { justification = ""; } // null justification -> empty justification

        // approved questions can have no justification
        if(!justification.isEmpty()) {
            checkJustification(justification);
        }

        StudentQuestion studentQuestion = findStudentQuestionById(studentQuestionId);

        studentQuestion.setSubmittedStatus(StudentQuestion.SubmittedStatus.APPROVED);
        studentQuestion.setJustification(justification);

        return new StudentQuestionDTO(studentQuestion);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDTO rejectStudentQuestion(Integer studentQuestionId, String justification) {
        checkJustification(justification);

        StudentQuestion studentQuestion = findStudentQuestionById(studentQuestionId);

        if(!canReject(studentQuestion)) {
            throw new TutorException(CANNOT_REJECT_ACCEPTED_SUGGESTION);
        }

        studentQuestion.setSubmittedStatus(StudentQuestion.SubmittedStatus.REJECTED);
        studentQuestion.setJustification(justification);
        return new StudentQuestionDTO(studentQuestion);
    }


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    private StudentQuestion findStudentQuestionById(Integer id) {
         return studentQuestionRepository.findById(id).orElseThrow(() -> new TutorException(STUDENT_QUESTION_NOT_FOUND, id));
    }

    private void checkJustification(String justification) {
        if (justification == null || justification.isBlank()) {
            throw new TutorException(INVALID_JUSTIFICATION, justification == null ? "" : justification);
        }
    }

    private boolean canReject(StudentQuestion studentQuestion) {
        // if already accepted throw error
        // change later. the question may exist in quizzes, etc
        return studentQuestion.getSubmittedStatus() != StudentQuestion.SubmittedStatus.APPROVED;
    }
}
