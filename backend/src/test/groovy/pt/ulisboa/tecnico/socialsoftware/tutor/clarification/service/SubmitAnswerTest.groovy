package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

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
class SubmitAnswerTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"

    static final String ANSWER_1 = "some answer 1"
    static final String ANSWER_2 = "some answer 2"

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
    ClarificationRequestAnswerRepository clarificationRequestAnswerRepository;

    @Autowired
    ClarificationService clarificationService

    Course course
    CourseExecution courseExecution
    Question question
    Quiz quiz
    QuizQuestion quizQuestion
    QuizAnswer quizAnswer
    User student
    User teacher
    ClarificationRequest clarificationRequest
    int studentId
    int teacherId
    int reqId

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        quiz = createQuiz(1, courseExecution, "GENERATED")
        question = createQuestion(1, course)
        quizQuestion = new QuizQuestion(quiz, question, 1)
        student = createStudent(1, "STUDENT", courseExecution)
        teacher = createTeacher(2, "TEACHER", courseExecution)
        quizAnswer = new QuizAnswer(student, quiz)

        def dto = new ClarificationRequestDto()
        dto.setKey(1)
        dto.setContent("some request")
        clarificationRequest = new ClarificationRequest(student, question, dto)
        student.addClarificationRequest(clarificationRequest)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        quizRepository.save(quiz)
        questionRepository.save(question)
        quizQuestionRepository.save(quizQuestion)
        userRepository.save(student)
        userRepository.save(teacher)
        quizAnswerRepository.save(quizAnswer)
        clarificationRequestRepository.save(clarificationRequest)

        studentId = student.getId()
        teacherId = teacher.getId()
        reqId = clarificationRequest.getId()
    }

    private User createStudent(int key, String name, CourseExecution courseExecution) {
        def u = new User()
        u.setKey(key)
        u.setName(name)
        u.setUsername(name)
        u.setRole(User.Role.STUDENT)
        u.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(u)
        return u
    }

    private User createTeacher(int key, String name, CourseExecution courseExecution) {
        def u = new User()
        u.setKey(key)
        u.setName(name)
        u.setUsername(name)
        u.setRole(User.Role.TEACHER)
        u.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(u)
        return u
    }

    private Question createQuestion(int key, Course course) {
        def question = new Question()
        question.setKey(key)
        question.setCourse(course)
        question.setTitle("TITLE")
        course.addQuestion(question)
        return question
    }

    private Quiz createQuiz(int key, CourseExecution courseExecution, String type) {
        def quiz = new Quiz()
        quiz.setKey(key)
        quiz.setType(type)
        quiz.setCourseExecution(courseExecution)
        courseExecution.addQuiz(quiz)
        return quiz
    }

    private CourseExecution createCourseExecution(Course course, String acronym, String term) {
        def courseExecution = new CourseExecution()
        courseExecution.setCourse(course)
        courseExecution.setAcronym(acronym)
        courseExecution.setAcademicTerm(term)
        return courseExecution
    }

    def "submit an answer"() {
        when:
        clarificationService.submitClarificationRequestAnswer(teacher, reqId, ANSWER_1)

        then: "the answer was submitted"
        clarificationRequestAnswerRepository.count() == 1
        def ans = clarificationRequestAnswerRepository.findAll().get(0)
        ans.getContent() == ANSWER_1
        ans.getCreator().getId() == teacherId
        ans.getRequest().getId() == reqId
    }

    def "submit answer to already answered request"() {
        given: "a clarification request that already has an answer"
        clarificationService.submitClarificationRequestAnswer(teacher, reqId, ANSWER_1)

        when:
        clarificationService.submitClarificationRequestAnswer(teacher, reqId, ANSWER_2)

        then: "the answer was replaced"
        def saved_answer = clarificationRequest.getAnswer().map({a -> a.getContent()}).get()
        saved_answer == ANSWER_2
    }

    @Unroll
    def "validity check: (validRequest=#validR, answer=#answer) -> #errorMessage"() {
        when: "submitting an answer for a null clarification request"
        def rid = validR ? reqId : -1
        clarificationService.submitClarificationRequestAnswer(teacher, rid, answer)

        then: "an exception"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        where:
        validR | answer     || errorMessage
        false  | ANSWER_1   || ErrorMessage.CLARIFICATION_REQUEST_NOT_FOUND
        true   | " \n  \t " || ErrorMessage.CLARIFICATION_REQUEST_ANSWER_MISSING_CONTENT
        true   | null       || ErrorMessage.CLARIFICATION_REQUEST_ANSWER_MISSING_CONTENT
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService();
        }
    }
}
