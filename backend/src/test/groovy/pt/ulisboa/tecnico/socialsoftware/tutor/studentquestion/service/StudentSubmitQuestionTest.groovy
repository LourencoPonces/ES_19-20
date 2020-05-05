package pt.ulisboa.tecnico.socialsoftware.tutor.studentquestion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentSubmitQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.ACCESS_DENIED
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.NO_TOPICS
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.NO_CORRECT_OPTIONS
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.NO_OPTIONS

@DataJpaTest
class StudentSubmitQuestionTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String URL = 'URL'
    public static final String TOPIC_NAME = "topic name"
    public static  final String TOPIC_NAME_2 = "topic name2"

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    StudentSubmitQuestionService studentSubmitQuestionService

    def user
    def course
    def courseExecution
    def topic

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course)
        topicRepository.save(topic)

        user = new User()
        user.setKey(1)
        user.setUsername("ist199999")
        user.setRole(User.Role.STUDENT)
        userRepository.save(user)

    }
     @Unroll
    def "invalid arguments where isTopic is #topic, isOption is #option, isCorrect is #correct and errorMessage is #errorMessage"() {
        given: "a questionDTO"
        def questionDTO = new StudentQuestionDTO();
        StudentQuestionDtoSetup(questionDTO)
        questionDTO.setStudentQuestionKey(1)

        setTopic(isTopic, questionDTO)
        setOption(isOption, isCorrect, questionDTO)
        setUser(isStudent)

        when:
        studentSubmitQuestionService.studentSubmitQuestion(course.getId(), questionDTO, user.getId())

        then:
        def error = thrown(TutorException)
        errorMessage == error.errorMessage

        where:
        isTopic | isOption | isCorrect |isStudent || errorMessage
        false   |    true  |     true  | true     || NO_TOPICS
        true    |   false  |   false   | true     || NO_OPTIONS
        true    |   true   |  false    | true     || NO_CORRECT_OPTIONS

    }

    def setTopic(isTopic, exampleQuestionDto) {
        if(isTopic) {
            def topicDto = new TopicDto(topic)
            def list = new ArrayList<TopicDto>()
            list.add(topicDto)
            exampleQuestionDto.setTopics(list)
        }
    }

    def setOption(isOption, isCorrect, exampleQuestionDto) {
        if(isOption) {
            def option = new OptionDto()
            option.setContent(OPTION_CONTENT)
            def list = new ArrayList<OptionDto>()

            if(isCorrect) {
                option.setCorrect(true);
            }
            list.add(option)
            exampleQuestionDto.setOptions(list)
        }
    }

    def setUser(boolean isUser) {
        if (isUser) {
            user.setRole(User.Role.STUDENT)
        } else {
            user.setRole(User.Role.TEACHER)
        }
    }

    def "create new suggestion with Image and two options and 2 Topics and submit for approval"() {
        given: "a questionDto"
        def questionDto = new StudentQuestionDTO()
        StudentQuestionDtoSetup(questionDto)
        questionDto.setStudentQuestionKey(1) // Specific to this test
        and: "2 Topics"
        def topic1Dto = new TopicDto(topic)
        def topic2 = new Topic()
        topic2.setName(TOPIC_NAME_2)
        topic2.setCourse(course)
        topicRepository.save(topic2)
        def topic2Dto = new TopicDto(topic2)
        def topicList = new ArrayList<TopicDto>()
        addToList(topicList, topic1Dto)
        addToList(topicList, topic2Dto)
        questionDto.setTopics(topicList)
        and: "2 Options"
        def option1 = new OptionDto()
        option1.setContent(OPTION_CONTENT)
        option1.setCorrect(true);
        def option2 = new OptionDto()
        option2.setContent(OPTION_CONTENT)
        option2.setCorrect(false)
        def optionList = new ArrayList<OptionDto>()
        addToList(optionList, option1)
        addToList(optionList, option2)
        questionDto.setOptions(optionList)
        and: "an Image"
        def image = new ImageDto()
        image.setUrl(URL)
        image.setWidth(20)
        questionDto.setImage(image)

        when:
        studentSubmitQuestionService.studentSubmitQuestion(course.getId(), questionDto, user.getId())

        then: "Student Question is created"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getStudentQuestionKey() == 1
        result.getSubmittedStatus() == StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getImage().getId() != null
        result.getImage().getUrl() == URL
        result.getImage().getWidth() == 20
        result.getOptions().size() == 2
        result.getUser().getId() == user.getId()
    }

    def StudentQuestionDtoSetup(StudentQuestionDTO questionDto) {
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setStatus(Question.Status.DISABLED.name())
    }

    def addToList(ArrayList list, Object o) {
        list.add(o)
    }

    def "create 2 new suggestions with 1 Topic each and submit for approval"() {
        given: " a StudentQuestionDTOs"
        def questionDto = new StudentQuestionDTO()
        StudentQuestionDtoSetup(questionDto)
        and: "a TopicDTO"
        def topicDto = new TopicDto(topic)
        def topicList = new ArrayList<TopicDto>()
        addToList(topicList, topicDto)
        questionDto.setTopics(topicList)
        and: "a OptionDTO"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def optionList = new ArrayList<OptionDto>()
        addToList(optionList, optionDto)
        questionDto.setOptions(optionList)

        when: "create 2 student questions"
        studentSubmitQuestionService.studentSubmitQuestion(course.getId(), questionDto, user.getId())
        questionDto.setStudentQuestionKey(null)
        studentSubmitQuestionService.studentSubmitQuestion(course.getId(), questionDto, user.getId())

        then: "the two student questions are created with the correct numbers"
        studentQuestionRepository.count() == 2L
        def resultOne = studentQuestionRepository.findAll().get(0)
        def resultTwo = studentQuestionRepository.findAll().get(1)
        resultOne.getStudentQuestionKey() + resultTwo.getStudentQuestionKey() == 3

    }

    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        StudentSubmitQuestionService studentSubmitQuestionService() {
            return new StudentSubmitQuestionService();
        }
    }

}


