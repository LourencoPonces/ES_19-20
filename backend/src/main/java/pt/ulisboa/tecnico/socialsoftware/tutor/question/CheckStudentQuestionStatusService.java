package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class CheckStudentQuestionStatusService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StudentQuestionRepository studentQuestionRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Stream<StudentQuestionDTO> getAllStudentQuestion(Integer studentId) {

        User user = userRepository.findById(studentId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, studentId));
        if(user.getRole() == User.Role.TEACHER) {
            throw new TutorException(ACCESS_DENIED);
        }
        return studentQuestionRepository.findByUser(studentId).map(StudentQuestionDTO::new);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<StudentQuestionDTO> findByCourseAndUser(Integer courseId, Integer studentId) {
        return studentQuestionRepository.findByCourseAndUser(courseId, studentId).stream().map(StudentQuestionDTO::new).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<StudentQuestionDTO> findByCourseUserAndStatus(Integer courseId, Integer studentId, StudentQuestion.SubmittedStatus status) {
        return studentQuestionRepository.findByCourseUserAndStatus(courseId, studentId, status).stream().map(StudentQuestionDTO::new).collect(Collectors.toList());
    }
}
