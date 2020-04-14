package pt.ulisboa.tecnico.socialsoftware.tutor.question;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository;

import java.sql.SQLException;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_NOT_FOUND;

@Service
public class RemoveStudentQuestionService {

    @Autowired
    private StudentQuestionRepository studentQuestionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void removeStudentQuestion(Integer studentQuestionId) {
        StudentQuestion studentQuestion = studentQuestionRepository.findById(studentQuestionId).orElseThrow(()->new TutorException(ErrorMessage.STUDENT_QUESTION_NOT_FOUND, studentQuestionId));
        studentQuestion.remove();
        studentQuestionRepository.delete(studentQuestion);
        questionRepository.delete(studentQuestion);
    }
    
    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDTO findStudentQuestionById(Integer studentQuestionId) {
        return studentQuestionRepository.findById(studentQuestionId).map(StudentQuestionDTO::new)
                .orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, studentQuestionId));
    }
}
