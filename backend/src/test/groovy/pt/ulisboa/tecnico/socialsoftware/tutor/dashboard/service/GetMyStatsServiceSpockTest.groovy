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
class GetMyStatsServiceSpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"
    static final String CONTENT = "Test Content"
    static final String USERNAME_1 = "USERNAME_ONE"
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
    Question question1
    Question question2
    Quiz quiz
    User student
    int studentId
    int courseId

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        def courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        createQuiz(1, courseExecution, "GENERATED")
        question1 = createQuestion(1, course)
        question2 = createQuestion(2, course)
        def quizQuestion1 = new QuizQuestion(quiz, question1, 1)
        def quizQuestion2 = new QuizQuestion(quiz, question2, 2)
        createStudent(courseExecution)
        def quizAnswer1 = new QuizAnswer(student, quiz)
        def quizAnswer2 = new QuizAnswer(student, quiz)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        quizRepository.save(quiz)
        questionRepository.save(question1)
        questionRepository.save(question2)
        quizQuestionRepository.save(quizQuestion1)
        quizQuestionRepository.save(quizQuestion2)
        userRepository.save(student)
        quizAnswerRepository.save(quizAnswer1)
        quizAnswerRepository.save(quizAnswer2)
        studentId = student.getId()
        courseId = course.getId()
    }

    private void createStudent(CourseExecution courseExecution) {
        student = new User('NAME', USERNAME_1, 1, User.Role.STUDENT)
        student.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(student)
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


    def "get user's dashboard stats"() {
        given: "a private clarification request about question 1"
        def request1 = new ClarificationRequestDto()
        request1.setContent(CONTENT)
        clarificationService.submitClarificationRequest(question1.getId(), studentId, request1)

        and: "a public clarification request about question 2"
        def request2 = new ClarificationRequestDto()
        request2.setContent(CONTENT)
        request2 = clarificationService.submitClarificationRequest(question2.getId(), studentId, request2)
        clarificationService.changeClarificationRequestStatus(request2.getId(), ClarificationRequest.RequestStatus.PUBLIC)

        when:
        def result = myStatsService.getMyStats(student.getUsername(), courseId)

        then:
        result != null
        result.getRequestsSubmittedStat() == 2
        result.getPublicRequestsStat() == 1
        result.getRequestsSubmittedVisibility() == MyStats.StatsVisibility.PRIVATE
        result.getPublicRequestsVisibility() == MyStats.StatsVisibility.PRIVATE
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
