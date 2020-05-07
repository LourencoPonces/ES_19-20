package pt.ulisboa.tecnico.socialsoftware.tutor.studentquestion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.RemoveStudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.ImageRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class RemoveStudentQuestionTest extends Specification {
    public static final String COURSE_NAME = "Arquitetura de Software"
    public static final String TOPIC_NAME = "topic name"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String URL = 'URL'
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"


    public static final String USER_NAME = "ist199999"

    @Autowired
    RemoveStudentQuestionService removeStudentQuestionService

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    ImageRepository imageRepository

    @Autowired
    OptionRepository optionRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    def course
    def user
    def topic
    def studentQuestion
    def optionOK
    def optionKO

    def setup() {
        course = new Course()
        course.setName(COURSE_NAME)
        courseRepository.save(course)

        def courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        user = createUser(courseExecution)
        userRepository.save(user)


        createTopic()
        setUpStudentQuestion();
    }

    def createTopic() {
        topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course)
        topicRepository.save(topic)
    }

    def setUpStudentQuestion() {
        studentQuestion =  new StudentQuestion();
        studentQuestion.addTopic(topic)
        studentQuestion.setKey(1);
        studentQuestion.setStudentQuestionKey(1);
        studentQuestion.setTitle(QUESTION_TITLE)
        studentQuestion.setContent(QUESTION_CONTENT)
        studentQuestion.setStatus(Question.Status.DISABLED)
        studentQuestion.setSubmittedStatus(StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL)
        studentQuestion.setNumberOfAnswers(2)
        studentQuestion.setNumberOfCorrect(1)
        studentQuestion.setCourse(course)
        studentQuestion.setUser(user)
        createOptions();
        createImage();
        course.addQuestion(studentQuestion)
        topic.addQuestion(studentQuestion)
        studentQuestionRepository.save(studentQuestion)
    }

    def createImage() {
        def image = new Image()
        image.setUrl(URL)
        image.setWidth(20)
        imageRepository.save(image)
        studentQuestion.setImage(image)
    }

    def createUser(CourseExecution courseExecution) {
        def user = new User()
        user.setKey(1)
        user.setUsername(USER_NAME)
        user.getCourseExecutions().add(courseExecution)
        user
    }

    def createOptions() {
        optionOK = new Option()
        optionOK.setContent(OPTION_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionRepository.save(optionOK)
        optionKO = new Option()
        optionKO.setContent(OPTION_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setSequence(1)
        optionRepository.save(optionKO)
        studentQuestion.addOption(optionOK)
        studentQuestion.addOption(optionKO)
    }


    def "remove a studentQuestion"() {
        when:
        removeStudentQuestionService.removeStudentQuestion(studentQuestion.getId());

        then: "the studentQuestion is removed"
        topicRepository.count() == 1L
        topic.getQuestions().size() == 0
        studentQuestionRepository.count() == 0L
        imageRepository.count() == 0L
        optionRepository.count() == 0L
    }

    def "remove a Rejected/Accepted question"() {
        given: "A status different from waiting for approval"
        studentQuestion.setSubmittedStatus(status);

        when:
        removeStudentQuestionService.removeStudentQuestion(studentQuestion.getId())

        then:
        def error = thrown(TutorException)
        error.getErrorMessage() == errorMessage

        where:
        status                                      || errorMessage
        StudentQuestion.SubmittedStatus.APPROVED    || ErrorMessage.QUESTION_ALREADY_READ
        StudentQuestion.SubmittedStatus.REJECTED    || ErrorMessage.QUESTION_ALREADY_READ
    }


    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        RemoveStudentQuestionService studentSubmitQuestionService() {
            return new RemoveStudentQuestionService();
        }
    }
}
