package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationMessageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationMessageRepository
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
class SubmitMessageSpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"

    static final ClarificationMessageDto MESSAGE_1 = new ClarificationMessageDto()
    static final ClarificationMessageDto MESSAGE_2 = new ClarificationMessageDto()

    static {
        MESSAGE_1.setContent("some msg 1")
        MESSAGE_2.setContent("some msg 2")
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
        clarificationRequest = new ClarificationRequest(question, student, dto)
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

    private static User createStudent(int key, String name, CourseExecution courseExecution) {
        def u = new User()
        u.setKey(key)
        u.setName(name)
        u.setUsername(name)
        u.setRole(User.Role.STUDENT)
        u.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(u)
        return u
    }

    private static User createTeacher(int key, String name, CourseExecution courseExecution) {
        def u = new User()
        u.setKey(key)
        u.setName(name)
        u.setUsername(name)
        u.setRole(User.Role.TEACHER)
        u.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(u)
        return u
    }

    private static Question createQuestion(int key, Course course) {
        def question = new Question()
        question.setKey(key)
        question.setCourse(course)
        question.setTitle("TITLE")
        course.addQuestion(question)
        return question
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
    def "submit a message as #teacherOrStudent"() {
        when:
        def user = teacherOrStudent == "teacher" ? teacher : student
        clarificationService.submitClarificationMessage(user, reqId, MESSAGE_1)

        then: "the message was submitted"
        clarificationMessageRepository.count() == 1
        def msg = clarificationMessageRepository.findAll().get(0)
        msg.content == MESSAGE_1.content
        msg.creator.id == user.getId()
        msg.request.id == reqId

        and: "message is associated to clarification request"
        clarificationRequest.messages.contains(msg)

        and: "message is associated to user"
        user.getClarificationMessages().stream()
                .anyMatch({ m -> m.content == msg.content })

        where:
        teacherOrStudent << ["teacher", "student"]
    }

    @Unroll
    def "submit second message to request as #teacherOrStudent"() {
        given: "a clarification request that already has a message"
        def user = teacherOrStudent == "teacher" ? teacher : student
        clarificationService.submitClarificationMessage(user, reqId, MESSAGE_1)

        when:
        clarificationService.submitClarificationMessage(user, reqId, MESSAGE_2)

        then: "the new message was submitted and is associated to the request"
        clarificationRequest.messages.size() == 2
        def savedMsg = clarificationRequest.messages[1]
        savedMsg.content == MESSAGE_2.content
        savedMsg.creator.id == user.id

        and: "message is associated to user"
        user.getClarificationMessages().stream()
                .anyMatch({ m -> m.content == MESSAGE_2.content })

        where:
        teacherOrStudent << ["teacher", "student"]
    }

    @Unroll
    def "validity check: (validRequest=#validR, answer=#answer) -> #errorMessage"() {
        when: "submitting an answer for a null clarification request"
        def rid = validR ? reqId : -1
        def msg = new ClarificationMessageDto()
        msg.setContent(msgContent)
        clarificationService.submitClarificationMessage(teacher, rid, msg)

        then: "an exception"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        where:
        validR | msgContent             || errorMessage
        false  | MESSAGE_1.getContent() || ErrorMessage.CLARIFICATION_REQUEST_NOT_FOUND
        true   | " \n  \t "             || ErrorMessage.CLARIFICATION_MESSAGE_MISSING_CONTENT
        true   | null                   || ErrorMessage.CLARIFICATION_MESSAGE_MISSING_CONTENT
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService()
        }
    }
}
