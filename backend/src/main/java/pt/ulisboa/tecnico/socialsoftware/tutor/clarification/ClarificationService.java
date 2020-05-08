package pt.ulisboa.tecnico.socialsoftware.tutor.clarification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationMessageDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestListDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationMessageRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import java.sql.SQLException;
import java.util.Set;


@Service
public class ClarificationService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClarificationRequestRepository clarificationRequestRepository;

    @Autowired
    private ClarificationMessageRepository clarificationMessageRepository;

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationRequestDto changeClarificationRequestStatus(int reqId, ClarificationRequest.RequestStatus status) {
        ClarificationRequest req = clarificationRequestRepository.findById(reqId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_REQUEST_NOT_FOUND));

        req.setStatus(status);
        return new ClarificationRequestDto(req);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationMessageDto submitClarificationMessage(int userId, int reqId, ClarificationMessageDto messageDto) {
        ClarificationRequest req = clarificationRequestRepository.findById(reqId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_REQUEST_NOT_FOUND));

        User user = getUser(userId);

        // Create message
        ClarificationMessage message = new ClarificationMessage(req, user, messageDto);

        req.getMessages().add(message);
        user.getClarificationMessages().add(message);

        // Update resolved flag
        if (messageDto.getResolved() != null)
            req.setResolved(messageDto.getResolved());

        clarificationMessageRepository.save(message);
        clarificationRequestRepository.save(req);
        userRepository.save(user);

        return new ClarificationMessageDto(message);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void deleteClarificationMessage(int userId, int messageId) {
        ClarificationMessage message = clarificationMessageRepository.findById(messageId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_MESSAGE_NOT_FOUND));

        ClarificationRequest request = message.getRequest();

        User user = getUser(userId);

        if (!message.getCreator().equals(user)) {
            throw new TutorException(ErrorMessage.ACCESS_DENIED);
        }

        request.getMessages().remove(message);
        user.getClarificationMessages().remove(message);

        userRepository.save(user);
        clarificationRequestRepository.save(request);
        clarificationMessageRepository.delete(message);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void deleteClarificationRequest(int studentId, int reqId) {
        ClarificationRequest req = clarificationRequestRepository.findById(reqId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_REQUEST_NOT_FOUND));

        User student = getStudent(studentId);

        if (!req.getCreator().getId().equals(student.getId())) {
            throw new TutorException(ErrorMessage.ACCESS_DENIED);
        }

        if (!req.getMessages().isEmpty()) {
            throw new TutorException(ErrorMessage.CLARIFICATION_REQUEST_NOT_EMPTY);
        }

        student.getClarificationRequests().remove(req);
        req.getQuestion().getClarificationRequests().remove(req);

        userRepository.save(student);
        questionRepository.save(req.getQuestion());
        clarificationRequestRepository.delete(req);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationRequestDto submitClarificationRequest(int questionId, int studentId, ClarificationRequestDto clarificationRequestDto) {
        User student = getStudent(studentId);

        checkIfDuplicate(questionId, student);

        Question question = tryGetAnsweredQuestion(questionId, student.getId());

        ClarificationRequest clarificationRequest = new ClarificationRequest(question, student, clarificationRequestDto);

        student.addClarificationRequest(clarificationRequest);
        question.addClarificationRequest(clarificationRequest);

        clarificationRequestRepository.save(clarificationRequest);
        userRepository.save(student);
        questionRepository.save(question);

        return new ClarificationRequestDto(clarificationRequest);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationRequestListDto getStudentClarificationRequests(int studentId) {
        User student = getStudent(studentId);
        return new ClarificationRequestListDto(student.getClarificationRequests());
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000)
    )
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationRequestListDto getTeacherClarificationRequests(int teacherId) {
        User teacher = getTeacher(teacherId);

        return new ClarificationRequestListDto(
                clarificationRequestRepository.getTeacherRequests(teacher.getId())
        );
    }

    private Question tryGetAnsweredQuestion(int questionId, int userId) {
        boolean answered = false;
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new TutorException(ErrorMessage.QUESTION_NOT_FOUND, questionId));
        Set<QuizQuestion> quizQuestions = question.getQuizQuestions();
        for (QuizQuestion qq : quizQuestions) {
            for (QuestionAnswer qa : qq.getQuestionAnswers()) {
                if (qa.getQuizAnswer().getUser().getId() == userId) {
                    answered = true;
                    break;
                }
            }
        }
        if (!answered) {
            throw new TutorException(ErrorMessage.QUESTION_NOT_ANSWERED_BY_STUDENT);
        }
        return question;
    }

    private User getUser(int studentId) {
        return userRepository.findById(studentId)
                .orElseThrow(() -> new TutorException(ErrorMessage.ACCESS_DENIED));
    }

    private User getStudent(int studentId) {
        User u = getUser(studentId);
        if (u.getRole() != User.Role.STUDENT) {
            throw new TutorException(ErrorMessage.ACCESS_DENIED);
        }

        return u;
    }

    private User getTeacher(int teacherId) {
        User u = getUser(teacherId);
        if (u.getRole() != User.Role.TEACHER) {
            throw new TutorException(ErrorMessage.ACCESS_DENIED);
        }

        return u;
    }

    private void checkIfDuplicate(int questionId, User user) {
        for (ClarificationRequest cr : user.getClarificationRequests()) {
            if (cr.getQuestion().getId() == questionId) {
                throw new TutorException(ErrorMessage.DUPLICATE_CLARIFICATION_REQUEST, user.getUsername());
            }
        }
    }

    public Integer findClarificationRequestCourseId(int requestId) {
        return clarificationRequestRepository.findById(requestId)
                .map(req -> req.getQuestion().getCourse().getId())
                .orElse(-1);
    }


}
