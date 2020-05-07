package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard.MyStats
import pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard.MyStatsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard.MyStatsService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class UpdateMyStatsVisibilitySpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"
    static final String USERNAME_1 = "USERNAME_ONE"

    public static final String OPTION_CONTENT = "optionId content"
    public static final String TOPIC_NAME = "topic name"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'


    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    MyStatsService myStatsService

    Course course
    CourseExecution courseExecution
    User student
    int studentId
    int courseId

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        student = createUser(courseExecution, User.Role.STUDENT, USERNAME_1, 1)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        userRepository.save(student)
        studentId = student.getId()
        courseId = course.getId()

    }

    private CourseExecution createCourseExecution(Course course, String acronym, String term) {
        def courseExecution = new CourseExecution()
        courseExecution.setCourse(course)
        courseExecution.setAcronym(acronym)
        courseExecution.setAcademicTerm(term)
        return courseExecution
    }

    private User createUser(CourseExecution courseExecution, User.Role role, String username, int key) {
        def user = new User('NAME', username, key, role)
        user.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(user)
        return user
    }

    private StudentQuestion createStudentQuestion(User user, Course course, int key) {
        def studentQuestion = new StudentQuestionDTO()
        studentQuestion.setTitle(QUESTION_TITLE)
        studentQuestion.setContent(QUESTION_CONTENT)
        studentQuestion.setStatus(Question.Status.DISABLED.name())
        studentQuestion.setKey(key)
        setTopics(studentQuestion)
        setOptions(studentQuestion)
        return new StudentQuestion(course, studentQuestion, user)
    }

    private void setOptions(StudentQuestionDTO studentQuestion) {
        def option = new OptionDto()
        option.setContent(OPTION_CONTENT)
        option.setCorrect(true)
        def optionList = new ArrayList<OptionDto>()
        optionList.add(option)
        studentQuestion.setOptions(optionList)
    }

    private void setTopics(StudentQuestionDTO studentQuestion) {
        def topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course)
        topicRepository.save(topic)
        def topicDto = new TopicDto(topic)
        def topicList = new ArrayList<TopicDto>()
        topicList.add(topicDto)
        studentQuestion.setTopics(topicList)
    }

    def "change submitted and approved questions visibility to public"() {
        given: "2 studentQuestions"
        def studentQuestion1 = createStudentQuestion(student, course, 1)
        student.addStudentQuestion(studentQuestion1)
        def studentQuestion2 = createStudentQuestion(student, course, 2)
        student.addStudentQuestion(studentQuestion2)

        and: "one of them approved"
        studentQuestion2.setSubmittedStatus(StudentQuestion.SubmittedStatus.APPROVED)
        studentQuestionRepository.save(studentQuestion1)
        studentQuestionRepository.save(studentQuestion2)

        and: "the stats changed to public"
        MyStatsDto myStatsDto = myStatsService.getMyStats(student.getId(), courseId)
        myStatsDto.setApprovedQuestionsVisibility(MyStats.StatsVisibility.PUBLIC)
        myStatsDto.setSubmittedQuestionsVisibility(MyStats.StatsVisibility.PUBLIC)

        when:
        def result = myStatsService.updateVisibility(myStatsDto.getId(), myStatsDto)


        then:
        result != null
        result.getSubmittedQuestionsStat() == 2
        result.getApprovedQuestionsStat() == 1
        result.getApprovedQuestionsVisibility() == MyStats.StatsVisibility.PUBLIC
        result.getSubmittedQuestionsVisibility() == MyStats.StatsVisibility.PUBLIC

    }

    def "change submitted and approved questions visibility to private"() {
        given: "2 studentQuestions"
        def studentQuestion1 = createStudentQuestion(student, course, 1)
        student.addStudentQuestion(studentQuestion1)
        def studentQuestion2 = createStudentQuestion(student, course, 2)
        student.addStudentQuestion(studentQuestion2)

        and: "one of them approved"
        studentQuestion2.setSubmittedStatus(StudentQuestion.SubmittedStatus.APPROVED)
        studentQuestionRepository.save(studentQuestion1)
        studentQuestionRepository.save(studentQuestion2)

        and: "the stats changed to private"
        MyStatsDto myStatsDto = myStatsService.getMyStats(student.getId(), courseId)
        myStatsDto.setApprovedQuestionsVisibility(MyStats.StatsVisibility.PRIVATE)
        myStatsDto.setSubmittedQuestionsVisibility(MyStats.StatsVisibility.PRIVATE)

        when:
        def result = myStatsService.updateVisibility(myStatsDto.getId(), myStatsDto)


        then:
        result != null
        result.getSubmittedQuestionsStat() == 2
        result.getApprovedQuestionsStat() == 1
        result.getApprovedQuestionsVisibility() == MyStats.StatsVisibility.PRIVATE
        result.getSubmittedQuestionsVisibility() == MyStats.StatsVisibility.PRIVATE

    }

    def "try to get stats that don't exist"() {
        def nonExistingMyStatsId = 100000
        def emptyDto = new MyStatsDto()

        when:
        myStatsService.updateVisibility(nonExistingMyStatsId, emptyDto)

        then: "Error is thrown"
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.NO_MY_STATS_FOUND
    }


    @TestConfiguration
    static class UpdateMyStatsVisibilityTestContextConfiguration {

        @Bean
        MyStatsService MyStatsService() {
            return new MyStatsService();
        }

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService();
        }
    }
}
