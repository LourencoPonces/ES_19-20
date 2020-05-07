package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.annotation.DirtiesContext
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationMessageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationMessageRepository
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
import spock.lang.Unroll

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SubmitMessagePerformanceTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"

    static final ClarificationMessageDto MESSAGE_1 = new ClarificationMessageDto()
    static final ClarificationMessageDto MESSAGE_2 = new ClarificationMessageDto()

    static {
        MESSAGE_1.setContent("some message 1")
        MESSAGE_2.setContent("some message 2")
    }

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
    ClarificationMessageRepository clarificationMessageRepository

    @Autowired
    QuestionAnswerRepository questionAnswerRepository

    @Autowired
    ClarificationService clarificationService

    Course course
    CourseExecution courseExecution
    User student
    User teacher

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        student = createUser(User.Role.STUDENT, 1, "STUDENT", courseExecution)
        teacher = createUser(User.Role.TEACHER, 2, "TEACHER", courseExecution)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        userRepository.save(teacher)
        userRepository.save(student)
    }

    private static User createUser(User.Role role, int key, String name, CourseExecution courseExecution) {
        def u = new User()
        u.setKey(key)
        u.setName(name)
        u.setUsername(name)
        u.setRole(role)
        u.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(u)
        return u
    }

    private static Question createQuestion(Course course) {
        def question = new Question()
        question.setCourse(course)
        question.setTitle("TITLE")
        course.addQuestion(question)
        return question
    }

    private static QuestionAnswer createQuestionAnswer(QuizAnswer quizAnswer, QuizQuestion quizQuestion) {
        def questionAnswer = new QuestionAnswer()
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswer.setSequence(0)

        return questionAnswer
    }

    private static Quiz createQuiz(int key, CourseExecution courseExecution, String type) {
        def quiz = new Quiz()
        quiz.setKey(key)
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


    @Unroll
    def "submit #count messages for different requests (for the first time)"() {
        given: "#count unanswered clarification requests"
        1.upto(count, {
            int i = it as int

            def question = createQuestion(course)
            def quiz = createQuiz(i, courseExecution, "GENERATED")
            def quizQuestion = new QuizQuestion(quiz, question, 1)
            def quizAnswer = new QuizAnswer(student, quiz)
            def questionAnswer = createQuestionAnswer(quizAnswer, quizQuestion)

            questionRepository.save(question)
            quizRepository.save(quiz)
            quizQuestionRepository.save(quizQuestion)
            quizAnswerRepository.save(quizAnswer)
            questionAnswerRepository.save(questionAnswer)

            def clarificationRequest = new ClarificationRequestDto()
            clarificationRequest.setContent("i need help with my performance")
            clarificationService.submitClarificationRequest(question.id, student.id, clarificationRequest)
        })


        when:
        1.upto(count, {
            clarificationService.submitClarificationMessage(teacher.id, it as int, MESSAGE_1)
        })

        then:
        true

        where:
        count = 1 // set to a big number like 1000 for a proper test
    }

    @Unroll
    def "submit #count messages for the same request"() {
        given: "#count unanswered clarification requests"

            def question = createQuestion(course)
            def quiz = createQuiz(1, courseExecution, "GENERATED")
            def quizQuestion = new QuizQuestion(quiz, question, 1)
            def quizAnswer = new QuizAnswer(student, quiz)
            def questionAnswer = createQuestionAnswer(quizAnswer, quizQuestion)

            questionRepository.save(question)
            quizRepository.save(quiz)
            quizQuestionRepository.save(quizQuestion)
            quizAnswerRepository.save(quizAnswer)
            questionAnswerRepository.save(questionAnswer)

            def clarificationRequest = new ClarificationRequestDto()
            clarificationRequest.setContent("i need help with my performance")
            clarificationRequest = clarificationService.submitClarificationRequest(question.id, student.id, clarificationRequest)


        when:
        1.upto(count, {
            clarificationService.submitClarificationMessage(teacher.id, clarificationRequest.id, MESSAGE_1)
        })

        then:
        true

        where:
        count = 1 // set to a big number like 1000 for a proper test
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService()
        }
    }
}
