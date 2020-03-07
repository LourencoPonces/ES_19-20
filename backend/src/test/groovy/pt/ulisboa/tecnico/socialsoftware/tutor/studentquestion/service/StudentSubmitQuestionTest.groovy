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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO
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
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    StudentSubmitQuestionService studentSubmitQuestionService

    def exampleUser
    def exampleCourse
    def courseExecution

    def setup() {
        exampleCourse = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(exampleCourse)

        courseExecution = new CourseExecution(exampleCourse, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        exampleUser = new User()
        exampleUser.setKey(1)
        exampleUser.setUsername("ist199999")
        userRepository.save(exampleUser)

    }
     @Unroll
    def "invalid arguments where isTopic is #topic, isOption is #option, isCorrect is #correct and errorMessage is #errorMessage"() {
        given: "a questionDTO"
        def exampleQuestionDto = new StudentQuestionDTO();
        exampleQuestionDto.setTitle(QUESTION_TITLE)
        exampleQuestionDto.setContent(QUESTION_CONTENT)
        exampleQuestionDto.setKey(1)
        exampleQuestionDto.setStatus(Question.Status.AVAILABLE.name())

        setTopic(isTopic, exampleQuestionDto)
        setOption(isOption, isCorrect, exampleQuestionDto)
        setUser(isStudent)

        when:
        studentSubmitQuestionService.studentSubmitQuestion(exampleCourse.getId(), exampleQuestionDto, exampleUser.getId())

        then:
        def error = thrown(TutorException)
        errorMessage == error.errorMessage

        where:
        isTopic | isOption | isCorrect |isStudent || errorMessage
        false   |    true  |     true  | true     || NO_TOPICS
        true    |   false  |   false   | true     || NO_OPTIONS
        true    |   true   |  false    | true     || NO_CORRECT_OPTIONS
        true    |   true   |  true     | false    || ACCESS_DENIED

    }

    def setTopic(isTopic, exampleQuestionDto) {
        if(isTopic) {
            def topic = new TopicDto()
            def list = new ArrayList<TopicDto>()
            topic.setName(TOPIC_NAME)
            list.add(topic)
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
            exampleUser.setRole(User.Role.STUDENT)
        } else {
            exampleUser.setRole(User.Role.TEACHER)
        }
    }

    def "create new suggestion with Image and two options and 2 Topics and submit for approval"() {
        given: "a questionDto"
        def questionDto = new StudentQuestionDTO()
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setKey(1)
        and: "2 Topics"
        def topic1 = new TopicDto()
        topic1.setName(TOPIC_NAME)
        def topic2 = new TopicDto()
        topic2.setName(TOPIC_NAME_2)
        def topicList = new ArrayList<TopicDto>()
        topicList.add(topic1)
        topicList.add(topic2)
        questionDto.setTopics(topicList)
        and: "2 Options"
        def option1 = new OptionDto()
        option1.setContent(OPTION_CONTENT)
        option1.setCorrect(true);
        def option2 = new OptionDto()
        option2.setContent(OPTION_CONTENT)
        option2.setCorrect(false)
        def optionList = new ArrayList<OptionDto>()
        optionList.add(option1)
        optionList.add(option2)
        questionDto.setOptions(optionList)
        and: "an Image"
        def image = new ImageDto()
        image.setUrl(URL)
        image.setWidth(20)
        questionDto.setImage(image)

        when:
        studentSubmitQuestionService.studentSubmitQuestion(exampleCourse.getId(), questionDto, exampleUser.getId())

        then: "Student Question is created"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getSubmittedStatus() == StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getImage().getId() != null
        result.getImage().getUrl() == URL
        result.getImage().getWidth() == 20
        result.getOptions().size() == 2
        result.getUser().getId() == exampleUser.getId()
    }

    def "create 2 new suggestions with 1 Topic each and submit for approval"() {
        given: " a StudentQuestionDTOs"
        def questionDto = new StudentQuestionDTO()
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setKey(1)
        and: "a TopicDTO"
        def topicDto = new TopicDto()
        topicDto.setName(TOPIC_NAME)
        def topicList = new ArrayList<TopicDto>()
        topicList.add(topicDto)
        questionDto.setTopics(topicList)
        and: "a OptionDTO"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def optionList = new ArrayList<OptionDto>()
        optionList.add(optionDto)
        questionDto.setOptions(optionList)

        when: "create 2 student questions"
        studentSubmitQuestionService.studentSubmitQuestion(exampleCourse.getId(), questionDto, exampleUser.getId())
        questionDto.setKey(null)
        studentSubmitQuestionService.studentSubmitQuestion(exampleCourse.getId(), questionDto, exampleUser.getId())

        then: "the two student questions are created with the correct numbers"
        studentQuestionRepository.count() == 2L
        def resultOne = studentQuestionRepository.findAll().get(0)
        def resultTwo = studentQuestionRepository.findAll().get(1)
        resultOne.getKey() + resultTwo.getKey() == 3

    }

    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        StudentSubmitQuestionService studentSubmitQuestionService() {
            return new StudentSubmitQuestionService();
        }
    }

}


