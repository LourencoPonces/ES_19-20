package pt.ulisboa.tecnico.socialsoftware.tutor.studentquestion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentSubmitQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class StudentSubmitQuestionProfilingTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String TOPIC_NAME = "topic name"

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

    def user
    def course
    def courseExecution

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        user = new User()
        user.setKey(1)
        user.setUsername("ist199999")
        user.setRole(User.Role.STUDENT)
        userRepository.save(user)

    }

    def StudentQuestionDtoSetup(StudentQuestionDTO questionDto) {
        questionDto.setContent(QUESTION_CONTENT)
        questionDto.setTitle(QUESTION_TITLE)
        questionDto.setStatus(Question.Status.DISABLED.name())
    }

    def addToList(ArrayList list, Object o) {
        list.add(o)
    }

    /* ===========================================
     * F1: Student submits student question
     * ===========================================
     */

    def "performance test to create 1000 student questions" () {
        def limit = 1 //USE 1000
        given: " a StudentQuestionDTOs"
        def questionDto = new StudentQuestionDTO()
        StudentQuestionDtoSetup(questionDto)
        and: "a TopicDTO"
        def topicDto = new TopicDto()
        topicDto.setName(TOPIC_NAME)
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

        when:
        1.upto(limit, {
            studentSubmitQuestionService.studentSubmitQuestion(course.getId(), questionDto, user.getId())
            questionDto.setStudentQuestionKey(null)})

        then:
        true
    }

    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        StudentSubmitQuestionService studentSubmitQuestionService() {
            return new StudentSubmitQuestionService();
        }
    }

}


