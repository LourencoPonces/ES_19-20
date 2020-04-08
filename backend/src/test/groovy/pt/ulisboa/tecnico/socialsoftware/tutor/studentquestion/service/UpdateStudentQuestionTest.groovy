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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentSubmitQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.ImageRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class UpdateStudentQuestionTest extends Specification{
    public static final String COURSE_NAME = "Arquitetura de Software"
    public static final String TOPIC_NAME = "topic name"
    public static final String NEW_TOPIC_NAME = "new topic name"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String NEW_OPTION_CONTENT = "new optionId content"
    public static final String NEW_QUESTION_TITLE = 'new question title'
    public static final String NEW_QUESTION_CONTENT = 'new question content'
    public static final String URL = 'URL'
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"


    public static final String USER_NAME = "ist199999"
    public static final String BAD_USER_NAME = "ist100000"

    @Autowired
    StudentSubmitQuestionService studentSubmitQuestionService

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
        user.setRole(User.Role.STUDENT)
        user.getCourseExecutions().add(courseExecution)
        userRepository.save(user)
    }

    def createOptions() {
        optionOK = new Option()
        optionOK.setContent(OPTION_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setQuestion(studentQuestion)
        optionRepository.save(optionOK)
        optionKO = new Option()
        optionKO.setContent(OPTION_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setQuestion(studentQuestion)
        optionRepository.save(optionKO)
        studentQuestion.addOption(optionOK)
        studentQuestion.addOption(optionKO)
    }


    def "update a student question"() {
        given: "a new StudentQuestion"
        def studentQuestionDto = new StudentQuestionDTO()
        studentQuestionDto.setId(studentQuestion.getId())
        studentQuestionDto.setTitle(NEW_QUESTION_TITLE)
        studentQuestionDto.setContent(NEW_QUESTION_CONTENT)
        studentQuestionDto.setStatus(Question.Status.DISABLED.name())
        studentQuestionDto.setSubmittedStatus(StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL)
        studentQuestionDto.setNumberOfAnswers(4)
        studentQuestionDto.setNumberOfCorrect(2)
        studentQuestionDto.setUser(USER_NAME)

        and: "a new Topic"
        def newTopic = new Topic()
        newTopic.setName(NEW_TOPIC_NAME)
        newTopic.setCourse(course)
        topicRepository.save(newTopic);
        def topicDto = new TopicDto(newTopic)
        def list = new ArrayList<TopicDto>()
        list.add(topicDto)
        studentQuestionDto.setTopics(list)

        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setId(optionOK.getId())
        optionDto.setContent(NEW_OPTION_CONTENT)
        optionDto.setCorrect(false)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setId(optionKO.getId())
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        options.add(optionDto)
        studentQuestionDto.setOptions(options)

        when:
        studentSubmitQuestionService.updateStudentQuestion(studentQuestionDto.getId(), studentQuestionDto, course.getId());

        then: "the student question is changed"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() == studentQuestionDto.getId()
        result.getTitle() == NEW_QUESTION_TITLE
        result.getContent() == NEW_QUESTION_CONTENT
        result.getTopics().size() == 1;
        result.getTopics().contains(newTopic)
        and: 'are not changed'
        result.getStatus() == Question.Status.DISABLED
        result.getSubmittedStatus() == StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL
        result.getNumberOfAnswers() == 2
        result.getNumberOfCorrect() == 1
        and: 'an option is changed'
        result.getOptions().size() == 2
        def resOptionOne = result.getOptions().stream().filter({option -> option.getId() == optionOK.getId()}).findAny().orElse(null)
        resOptionOne.getContent() == NEW_OPTION_CONTENT
        !resOptionOne.getCorrect()
        def resOptionTwo = result.getOptions().stream().filter({option -> option.getId() == optionKO.getId()}).findAny().orElse(null)
        resOptionTwo.getContent() == OPTION_CONTENT
        resOptionTwo.getCorrect()
    }

    def "update a student question with missing data"() {
        given: "Missing data"
        def studentQuestionDto =  new StudentQuestionDTO()
        studentQuestionDto.setId(studentQuestion.getId())
        studentQuestionDto.setTitle(NEW_QUESTION_TITLE)
        studentQuestionDto.setContent(NEW_QUESTION_CONTENT)
        studentQuestionDto.setUser(userName)
        studentQuestionDto.setStatus(Question.Status.DISABLED.name())
        studentQuestionDto.setSubmittedStatus(status)
        def list = new ArrayList<TopicDto>()
        addTopic(toAdd, list)
        studentQuestionDto.setTopics(list);

        when:
        studentSubmitQuestionService.updateStudentQuestion(studentQuestionDto.getId(), studentQuestionDto, course.getId())

        then:
        def error = thrown(TutorException)
        error.getErrorMessage() == errorMessage

        where:
        status                                               | toAdd            |           userName          || errorMessage
        StudentQuestion.SubmittedStatus.APPROVED             |  true            |             USER_NAME       || ErrorMessage.DIFFERENT_STATUS
        StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL |  false           |             USER_NAME       || ErrorMessage.NO_TOPICS
        StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL |  true            |            BAD_USER_NAME    || ErrorMessage.ACCESS_DENIED
    }

    def addTopic(toAdd, list) {
        if(toAdd) {
            list.add(new TopicDto(topic))
        }
    }

    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {
        @Bean
        StudentSubmitQuestionService studentSubmitQuestionService() {
            return new StudentSubmitQuestionService()
        }
    }
}
