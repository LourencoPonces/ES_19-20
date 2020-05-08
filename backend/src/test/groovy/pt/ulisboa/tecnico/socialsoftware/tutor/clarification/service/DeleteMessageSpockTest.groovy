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

@DataJpaTest
class DeleteMessageSpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"

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


    def "remove an answer"() {
        given: "answered clarification request"
        def msgDto = new ClarificationMessageDto()
        msgDto.setContent("some answer")
        msgDto = clarificationService.submitClarificationMessage(teacher.id, reqId, msgDto)

        when: "message is removed"
        clarificationService.deleteClarificationMessage(teacher.id, msgDto.id)

        then: "clarification request has no messages"
        clarificationRequest.getMessages().isEmpty()

        and: "message was deleted"
        clarificationMessageRepository.count() == 0

        and: "user doesn't have the message"
        teacher.getClarificationMessages().stream()
                .noneMatch({ m -> m.content == msgDto.content })

        and: "clarification request still exists"
        clarificationRequestRepository.count() == 1
    }

    def "don't remove non-existent things"() {
        when: "non existent message is removed"
        clarificationService.deleteClarificationMessage(student.id, 404)

        then: "thrown exception"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CLARIFICATION_MESSAGE_NOT_FOUND
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService()
        }
    }
}
