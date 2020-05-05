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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


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
    public ClarificationMessageDto submitClarificationMessage(User user, int reqId, ClarificationMessageDto messageDto) {
        ClarificationRequest req = clarificationRequestRepository.findById(reqId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_REQUEST_NOT_FOUND));

        // Create message
        ClarificationMessage message = new ClarificationMessage(req, user, messageDto);

        req.getMessages().add(message);
        user.getClarificationMessages().add(message);

        // Update resolved flag
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
    public void deleteClarificationMessage(User user, int messageId) {
        ClarificationMessage message = clarificationMessageRepository.findById(messageId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_MESSAGE_NOT_FOUND));

        ClarificationRequest request = message.getRequest();

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
    public void deleteClarificationRequest(int userId, int reqId) {
        User student = getStudent(userId);
        ClarificationRequest req = clarificationRequestRepository.findById(reqId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_REQUEST_NOT_FOUND));

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
    public ClarificationRequestDto submitClarificationRequest(int questionId, int userId, ClarificationRequestDto clarificationRequestDto) {
        User user = getStudent(userId);

        checkIfDuplicate(questionId, user);

        Question question = tryGetAnsweredQuestion(questionId, userId);

        ClarificationRequest clarificationRequest = new ClarificationRequest(question, user, clarificationRequestDto);

        user.addClarificationRequest(clarificationRequest);
        question.addClarificationRequest(clarificationRequest);

        clarificationRequestRepository.save(clarificationRequest);
        userRepository.save(user);
        questionRepository.save(question);

        return new ClarificationRequestDto(clarificationRequest);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ClarificationRequestDto> getStudentClarificationRequests(int userId) {
        User user = getStudent(userId);
        return user.getClarificationRequests()
                .stream()
                .map(ClarificationRequestDto::new)
                .sorted(Comparator.comparing(ClarificationRequestDto::getId).reversed())
                .collect(Collectors.toList());
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000)
    )
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ClarificationRequestDto> getTeacherClarificationRequests(int teacherId) {
        return clarificationRequestRepository.getTeacherRequests(teacherId)
                .map(ClarificationRequestDto::new)
                .collect(Collectors.toList());
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

    private void checkIfDuplicate(int questionId, User user) {
        for (ClarificationRequest cr : user.getClarificationRequests()) {
            if (cr.getQuestion().getId() == questionId) {
                throw new TutorException(ErrorMessage.DUPLICATE_CLARIFICATION_REQUEST, user.getUsername());
            }
        }
    }

    private User getStudent(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, userId));

        if (user.getRole() != User.Role.STUDENT) {
            throw new TutorException(ErrorMessage.ACCESS_DENIED);
        }
        return user;
    }

    public Integer findClarificationRequestCourseId(int requestId) {
        return clarificationRequestRepository.findById(requestId)
                .map(req -> req.getQuestion().getCourse().getId())
                .orElse(-1);
    }


}
