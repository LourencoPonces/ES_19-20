package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class TeacherEvaluatesStudentQuestionService {

    @Autowired
    private StudentQuestionRepository studentQuestionRepository;

    @Autowired
    private TopicRepository topicRepository;

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
    public StudentQuestionDTO evaluateStudentQuestion(Integer studentQuestionId, StudentQuestion.SubmittedStatus status, String justification) {
        StudentQuestion studentQuestion = findStudentQuestionById(studentQuestionId);

        studentQuestion.evaluate(status, justification);
        return new StudentQuestionDTO(studentQuestion);
    }


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    private StudentQuestion findStudentQuestionById(Integer id) {
         return studentQuestionRepository.findById(id).orElseThrow(() -> new TutorException(STUDENT_QUESTION_NOT_FOUND, id));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDTO updateAndPromoteStudentQuestion(Integer courseId, Integer studentQuestionId, StudentQuestionDTO changedStudentQuestionDTO) {
        // get question
        StudentQuestion studentQuestion = studentQuestionRepository.findById(studentQuestionId).orElseThrow(() -> new TutorException(STUDENT_QUESTION_NOT_FOUND, studentQuestionId));

        // set topics
        TopicDto[] topicArray = new TopicDto[changedStudentQuestionDTO.getTopics().size()];
        Set<Topic> newTopics = Arrays.stream(changedStudentQuestionDTO.getTopics().toArray(topicArray)).map(topicDto -> topicRepository.findTopicByName(courseId, topicDto.getName())).collect(Collectors.toSet());

        // update question
        studentQuestion.update(changedStudentQuestionDTO, newTopics);

        // evaluate
        studentQuestion.evaluate(StudentQuestion.SubmittedStatus.PROMOTED, changedStudentQuestionDTO.getJustification());

        return new StudentQuestionDTO(studentQuestion);
    }
}
