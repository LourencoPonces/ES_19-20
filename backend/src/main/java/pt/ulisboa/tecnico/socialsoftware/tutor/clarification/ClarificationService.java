package pt.ulisboa.tecnico.socialsoftware.tutor.clarification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Set;



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
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ClarificationRequestDto submitClarificationRequest(String text, int questionId, int userId, ClarificationRequestDto clarificationRequestDto) {
        User user = getStudent(userId);

        checkIfDuplicate(questionId, user);

        Question question = tryGetAnsweredQuestion(questionId, userId);

        ClarificationRequest clarificationRequest = createClarificationRequest(text, user, question, clarificationRequestDto);
        entityManager.persist(clarificationRequest);

        user.addClarificationRequest(clarificationRequest);
        entityManager.persist(user);

        return new ClarificationRequestDto(clarificationRequest);
    }

    private ClarificationRequest createClarificationRequest(String text, User user, Question question, ClarificationRequestDto clarificationRequestDto) {
        if (clarificationRequestDto.getKey() == null) {
            int max = clarificationRequestRepository.getMaxClarificationRequestKey() != null ?
                    clarificationRequestRepository.getMaxClarificationRequestKey() : 0;
            clarificationRequestDto.setKey(max + 1);
        }

        clarificationRequestDto.setOwner(user.getId());
        clarificationRequestDto.setQuestionId(question.getId());
        clarificationRequestDto.setContent(text);
        ClarificationRequest clarificationRequest = new ClarificationRequest(user, question, clarificationRequestDto);

        if (clarificationRequestDto.getCreationDate() == null) {
            clarificationRequest.setCreationDate(LocalDateTime.now());
        } else {
            clarificationRequest.setCreationDate(clarificationRequestDto.getCreationDateDate());
        }
        return clarificationRequest;
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

    private User getStudent(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, userId));

        if (user.getRole() != User.Role.STUDENT) {
            throw new TutorException(ErrorMessage.ACCESS_DENIED);
        }
        return user;
    }
}
