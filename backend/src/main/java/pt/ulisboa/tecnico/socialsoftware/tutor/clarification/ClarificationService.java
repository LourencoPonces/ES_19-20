package pt.ulisboa.tecnico.socialsoftware.tutor.clarification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequestAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
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

    @PersistenceContext
    EntityManager entityManager;


    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationRequestAnswerDto getClarificationRequestAnswer(int userId, int requestId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(ErrorMessage.AUTHENTICATION_ERROR));

        ClarificationRequest clarificationRequest = clarificationRequestRepository.findById(requestId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_REQUEST_NOT_SUBMITTED, user.getUsername()));

        if (user.getRole() == User.Role.STUDENT && !clarificationRequest.getOwner().getId().equals(user.getId())) {
            throw new TutorException(ErrorMessage.CLARIFICATION_REQUEST_NOT_SUBMITTED, user.getUsername());
        }

        ClarificationRequestAnswer answer = clarificationRequest.getAnswer()
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_REQUEST_UNANSWERED));

        return new ClarificationRequestAnswerDto(answer);

    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationRequestAnswerDto submitClarificationRequestAnswer(User teacher, int reqId, String answerText) {
        ClarificationRequest req = clarificationRequestRepository.findById(reqId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_REQUEST_NOT_FOUND, reqId));

        // Create/update answer
        ClarificationRequestAnswer ans = req.getAnswer().orElseGet(ClarificationRequestAnswer::new);
        ans.setContent(answerText);
        ans.setCreationDate(LocalDateTime.now());
        ans.setCreator(teacher);
        ans.setRequest(req);

        req.setAnswer(ans);

        entityManager.persist(ans);
        entityManager.persist(req);

        return new ClarificationRequestAnswerDto(ans);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void deleteClarificationRequestAnswer(User teacher, int reqId) {
        ClarificationRequest req = clarificationRequestRepository.findById(reqId)
                .orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_REQUEST_NOT_FOUND, reqId));

        ClarificationRequestAnswer ans = req.getAnswer().orElseThrow(() -> new TutorException(ErrorMessage.CLARIFICATION_REQUEST_UNANSWERED));

        req.removeAnswer();
        entityManager.persist(req);

        entityManager.remove(ans);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationRequestDto submitClarificationRequest(int questionId, int userId, ClarificationRequestDto clarificationRequestDto) {
        User user = getStudent(userId);

        checkIfDuplicate(questionId, user);

        Question question = tryGetAnsweredQuestion(questionId, userId);

        ClarificationRequest clarificationRequest = createClarificationRequest(user, question, clarificationRequestDto);
        entityManager.persist(clarificationRequest);

        user.addClarificationRequest(clarificationRequest);
        entityManager.persist(user);

        question.addClarificationRequest(clarificationRequest);
        entityManager.persist(question);

        return new ClarificationRequestDto(clarificationRequest);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<ClarificationRequestDto> getStudentClarificationRequests(int userId) {
        User user = getStudent(userId);
        return user.getClarificationRequests().stream().map(ClarificationRequestDto::new).collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    private ClarificationRequest createClarificationRequest(User user, Question question, ClarificationRequestDto clarificationRequestDto) {
        clarificationRequestDto.setOwner(user.getId());
        clarificationRequestDto.setQuestionId(question.getId());
        ClarificationRequest clarificationRequest = new ClarificationRequest(user, question, clarificationRequestDto);

        if (clarificationRequestDto.getCreationDate() == null) {
            clarificationRequest.setCreationDate(LocalDateTime.now());
        } else {
            clarificationRequest.setCreationDate(clarificationRequestDto.getCreationDateDate());
        }
        return clarificationRequest;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
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
            throw new TutorException(ErrorMessage.QUESTION_NOT_ANSWERED_BY_STUDENT, questionId, userId);
        }
        return question;
    }

    private void checkIfDuplicate(int questionId, User user) {
        for (ClarificationRequest cr : user.getClarificationRequests()) {
            if (cr.getQuestion().getId() == questionId) {
                throw new TutorException(ErrorMessage.DUPLICATE_CLARIFICATION_REQUEST, user.getUsername(), questionId);
            }
        }
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
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
