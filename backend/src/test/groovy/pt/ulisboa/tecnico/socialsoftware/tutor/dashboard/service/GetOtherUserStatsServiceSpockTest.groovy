package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard.MyStats
import pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard.MyStatsService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

@DataJpaTest
class GetOtherUserStatsServiceSpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"
    static final String CONTENT = "Test Content"
    static final String USERNAME_1 = "USERNAME_ONE"
    static final String USERNAME_2 = "USERNAME_TWO"
    static final String INVALID_USERNAME = "INVALID_USERNAME"
    static final int INVALID_COURSE_ID = 50

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    QuizQuestionRepository quizQuestionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    ClarificationRequestRepository clarificationRequestRepository

    @Autowired
    ClarificationRequestAnswerRepository clarificationRequestAnswerRepository

    @Autowired
    MyStatsService myStatsService

    @Autowired
    ClarificationService clarificationService


    Course course
    CourseExecution courseExecution
    Question question
    Quiz quiz
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

    def clarificationRequestSetup() {
        createQuiz(1, courseExecution, "GENERATED")
        question = createQuestion(1, course)
        def quizQuestion = new QuizQuestion(quiz, question, 1)
        def quizAnswer = new QuizAnswer(student, quiz)

        quizRepository.save(quiz)
        questionRepository.save(question)
        quizQuestionRepository.save(quizQuestion)
        quizAnswerRepository.save(quizAnswer)
    }

    private User createUser(CourseExecution courseExecution, User.Role role, String username, int key) {
        def user = new User('NAME', username, key, role)
        user.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(user)
        return user
    }

    private Question createQuestion(int key, Course course) {
        def question = new Question()
        question.setKey(key)
        question.setCourse(course)
        question.setTitle("TITLE")
        course.addQuestion(question)
        return question
    }

    private void createQuiz(int key, CourseExecution courseExecution, String type) {
        quiz = new Quiz()
        quiz.setKey(key)
        quiz.setType(type)
        quiz.setCourseExecution(courseExecution)
        courseExecution.addQuiz(quiz)
    }

    private CourseExecution createCourseExecution(Course course, String acronym, String term) {
        def courseExecution = new CourseExecution()
        courseExecution.setCourse(course)
        courseExecution.setAcronym(acronym)
        courseExecution.setAcademicTerm(term)
        return courseExecution
    }


    def "get other user's dashboard stats"() {
        clarificationRequestSetup()
        given: "a public clarification request by the first student"
        def request = new ClarificationRequestDto()
        request.setContent(CONTENT)
        request = clarificationService.submitClarificationRequest(question.getId(), studentId, request)
        clarificationService.changeClarificationRequestStatus(request.getId(), ClarificationRequest.RequestStatus.PUBLIC)

        when:
        def result = myStatsService.getOtherUserStats(student.getUsername(), courseId)

        then:
        result != null
        result.getRequestsSubmittedStat() == null
        result.getPublicRequestsStat() == null
    }

    def "get other user's student question dashboard stats"() {
        given: "a new student"
        def newStudent = createUser(courseExecution, User.Role.STUDENT, USERNAME_2, 2)
        userRepository.save(newStudent)

        when:
        def result = myStatsService.getOtherUserStats(student.getUsername(), courseId)

        then:
        result != null
        result.getSubmittedQuestionsVisibility() == MyStats.StatsVisibility.PRIVATE
        result.getSubmittedQuestionsStat() == null
        result.getApprovedQuestionsVisibility() == MyStats.StatsVisibility.PRIVATE
        result.getApprovedQuestionsStat() == null

    }

    @Unroll("invalid arguments: #isUsername | #isCourseid || #error_message")
    def "invalid arguments"() {
        when:
        changeCourseId(isCourseId)
        myStatsService.getMyStats(getUsername(isUsername), courseId)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == error_message

        where:
        isUsername | isCourseId  || error_message
        false      | true        || ErrorMessage.USERNAME_NOT_FOUND
        true       | false       || ErrorMessage.COURSE_NOT_FOUND
    }

    private String getUsername(boolean isUsername) {
        if (!isUsername)
            return INVALID_USERNAME
        return student.getUsername()
    }

    private void changeCourseId(boolean isCourseId) {
        if (!isCourseId)
            courseId = INVALID_COURSE_ID
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

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
