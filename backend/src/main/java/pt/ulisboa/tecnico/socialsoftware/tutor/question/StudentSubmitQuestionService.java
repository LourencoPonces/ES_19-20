package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class StudentSubmitQuestionService {

    @Autowired
    private StudentQuestionRepository studentQuestionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @PersistenceContext
    EntityManager entityManager;


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDTO studentSubmitQuestion(int courseId, StudentQuestionDTO studentQuestionDTO, int studentId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new TutorException(COURSE_NOT_FOUND, courseId));

        if (studentQuestionDTO.getStudentQuestionKey() == null) {
            int maxStudentQuestionNumber = studentQuestionRepository.getMaxQuestionNumberByUser(studentId) != null ?
                    studentQuestionRepository.getMaxQuestionNumberByUser(studentId) : 0;

            studentQuestionDTO.setStudentQuestionKey(maxStudentQuestionNumber + 1);
        }

        if (studentQuestionDTO.getCreationDate() == null) {
            studentQuestionDTO.setCreationDate(LocalDateTime.now().format(Course.formatter));
        }


        User student = userRepository.findById(studentId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, studentId));
        StudentQuestion studentQuestion = new StudentQuestion(course, studentQuestionDTO, student);

        for(TopicDto topicDto: studentQuestionDTO.getTopics()) {
            Topic t = topicRepository.findTopicByName(courseId, topicDto.getName());
            if (t == null) {
                throw new TutorException(TOPIC_NOT_FOUND, topicDto.getName());
            } else {
                studentQuestion.addTopic(t);
                t.getQuestions().add(studentQuestion);
            }
        }
        student.addStudentQuestion(studentQuestion);
        this.entityManager.persist(studentQuestion);

        return new StudentQuestionDTO(studentQuestion);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDTO updateStudentQuestion(Integer studentQuestionId, StudentQuestionDTO studentQuestionDTO, Integer courseId) {
        StudentQuestion studentQuestion = studentQuestionRepository.findById(studentQuestionId).orElseThrow(() -> new TutorException(STUDENT_QUESTION_NOT_FOUND, studentQuestionId));
        TopicDto[] topicArray = new TopicDto[studentQuestionDTO.getTopics().size()];
        Set<Topic> newTopics = Arrays.stream(studentQuestionDTO.getTopics().toArray(topicArray)).map(topicDto -> topicRepository.findTopicByName(courseId, topicDto.getName())).collect(Collectors.toSet());
        studentQuestion.update(studentQuestionDTO, newTopics);
        return new StudentQuestionDTO(studentQuestion);
    }
}
