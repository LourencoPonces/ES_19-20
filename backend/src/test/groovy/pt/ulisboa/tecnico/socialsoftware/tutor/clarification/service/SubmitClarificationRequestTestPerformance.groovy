package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.format.DateTimeFormatter

@DataJpaTest
class SubmitClarificationRequestTestPerformance extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"
    static final String CONTENT = "This is a test request."
    static final String USERNAME_ONE = "STUDENT_ONE"
    static final String NAME = "NAME"
    static final int KEY_ONE = 1
    static final int TEST_COUNT = 2 //test with 2000

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
    QuestionAnswerRepository questionAnswerRepository

    @Autowired
    ClarificationRequestRepository clarificationRequestRepository

    @Autowired
    ClarificationService clarificationService


    Course course
    CourseExecution courseExecution
    Question question
    Quiz quiz
    QuizQuestion quizQuestion
    QuizAnswer quizAnswer
    QuestionAnswer questionAnswer
    User student
    DateTimeFormatter formatter

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        courseExecutionRepository.save(courseExecution)

        student = createStudent(KEY_ONE, NAME, USERNAME_ONE, courseExecution)
        userRepository.save(student)

        1.upto(TEST_COUNT, {
            int i = it as int
            question = createQuestion(course)
            quiz = createQuiz(i, courseExecution, "GENERATED")
            quizQuestion = new QuizQuestion(quiz, question, 1)
            quizAnswer = new QuizAnswer(student, quiz)
            questionAnswer = createQuestionAnswer(quizAnswer, quizQuestion)

            quizRepository.save(quiz)
            questionRepository.save(question)
            quizQuestionRepository.save(quizQuestion)
            quizAnswerRepository.save(quizAnswer)
            questionAnswerRepository.save(questionAnswer)

        })
    }

    private static QuestionAnswer createQuestionAnswer(QuizAnswer quizAnswer, QuizQuestion quizQuestion) {
        def questionAnswer = new QuestionAnswer()
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswer.setSequence(0)

        return questionAnswer
    }

    private static User createStudent(int key, String name, String username, CourseExecution courseExecution) {
        def student = new User()
        student.setKey(key)
        student.setName(name)
        student.setUsername(username)
        student.setRole(User.Role.STUDENT)
        student.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(student)
        return student
    }

    private static Question createQuestion(Course course) {
        def question = new Question()
        question.setCourse(course)
        question.setTitle("TITLE")
        course.addQuestion(question)
        return question
    }

    private static Quiz createQuiz(int i, CourseExecution courseExecution, String type) {
        def quiz = new Quiz()
        quiz.setKey(i)
        quiz.setType(type)
        quiz.setCourseExecution(courseExecution)
        courseExecution.addQuiz(quiz)
        return quiz
    }

    private static CourseExecution createCourseExecution(Course course, String acronym, String term) {
        def courseExecution = new CourseExecution()
        courseExecution.setCourse(course)
        courseExecution.setAcronym(acronym)
        courseExecution.setAcademicTerm(term)
        return courseExecution
    }

    private static ClarificationRequestDto createRequestDto() {
        def clarificationRequest = new ClarificationRequestDto()
        clarificationRequest.setContent(CONTENT)

        return clarificationRequest
    }

    def "submit 2000 requests to 2000 different questions"() {
        when:
        1.upto(TEST_COUNT, {
            def clarificationRequestDto = createRequestDto()
            clarificationService.submitClarificationRequest(it as int, student.id, clarificationRequestDto)
        })

        then:
        true
    }

    def "student answered 2000 quizzes and submits one clarification request"() {
        when:
        int questionId = 4   // test with number between 1 and TEST_COUNT
        def clarificationRequestDto = createRequestDto()
        clarificationService.submitClarificationRequest(questionId, student.id, clarificationRequestDto)

        then:
        true
    }


    @TestConfiguration
    static class ClarificationServicePerformanceTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService()
        }
    }
}