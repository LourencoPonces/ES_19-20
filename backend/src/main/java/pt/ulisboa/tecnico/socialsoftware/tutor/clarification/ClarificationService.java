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
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

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
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        // check that is a student
        if (user.getRole() != User.Role.STUDENT) {
            throw new TutorException(ACCESS_DENIED);
        }

        // check that is not duplicate
        for (ClarificationRequest cr : user.getClarificationRequests()) {
            if (cr.getQuestion().getId() == questionId) {
                throw new TutorException(DUPLICATE_CLARIFICATION_REQUEST, user.getUsername(), questionId);
            }
        }

        // TODO check this
        // check that question is answered
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
            throw new TutorException(QUESTION_NOT_ANSWERED_BY_STUDENT, questionId, userId);
        }

        if (clarificationRequestDto.getKey() == null) {
            int max = clarificationRequestRepository.getMaxClarificationRequestKey() != null ?
                    clarificationRequestRepository.getMaxClarificationRequestKey() : 0;
            clarificationRequestDto.setKey(max + 1);
        }

        clarificationRequestDto.setOwner(userId);
        clarificationRequestDto.setQuestionId(questionId);
        clarificationRequestDto.setContent(text);
        ClarificationRequest clarificationRequest = new ClarificationRequest(user, question, clarificationRequestDto);

        if (clarificationRequestDto.getCreationDate() == null) {
            clarificationRequest.setCreationDate(LocalDateTime.now());
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            clarificationRequest.setCreationDate(LocalDateTime.parse(clarificationRequestDto.getCreationDate(), formatter));
        }

        entityManager.persist(clarificationRequest);

        user.addClarificationRequest(clarificationRequest);
        entityManager.persist(user);

        return new ClarificationRequestDto(clarificationRequest);
    }
}
