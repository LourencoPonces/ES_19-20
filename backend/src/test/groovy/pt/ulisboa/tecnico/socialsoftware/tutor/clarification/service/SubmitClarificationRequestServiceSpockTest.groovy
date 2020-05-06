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
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

import java.time.format.DateTimeFormatter

@DataJpaTest
class SubmitClarificationRequestServiceSpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"
    static final String CONTENT = "This is a test request."
    static final int INEXISTENT_QUESTION_ID = -1

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
    ClarificationService clarificationService

    Course course
    CourseExecution courseExecution
    Question question
    Quiz quiz
    QuizQuestion quizQuestion
    QuizAnswer quizAnswer
    User student, student2, teacher
    ClarificationRequestDto clarificationRequestDto
    DateTimeFormatter formatter
    int studentId
    int questionId

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        quiz = createQuiz(1, courseExecution, "GENERATED")
        question = createQuestion(1, course)
        quizQuestion = new QuizQuestion(quiz, question, 1)
        student = createUser(1, Role.STUDENT, "student one", courseExecution)
        student2 = createUser(2, Role.STUDENT, "student two", courseExecution)
        teacher = createUser(3, Role.TEACHER, "teacher", courseExecution)
        quizAnswer = new QuizAnswer(student, quiz)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        quizRepository.save(quiz)
        questionRepository.save(question)
        quizQuestionRepository.save(quizQuestion)
        userRepository.save(student)
        userRepository.save(student2)
        userRepository.save(teacher)
        quizAnswerRepository.save(quizAnswer)

        clarificationRequestDto = new ClarificationRequestDto()
        questionId = question.getId()
        studentId = student.getId()
    }

    private static User createUser(int key, User.Role role, String username, CourseExecution courseExecution) {
        def student = new User()
        student.setKey(key)
        student.setName(username)
        student.setUsername(username)
        student.setRole(role)
        student.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(student)
        return student
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

    def "the question has been answered and submit request"() {
        //the clarification request is created
        when:
        clarificationRequestDto.setContent(CONTENT)
        clarificationRequestDto = clarificationService.submitClarificationRequest(questionId, student, clarificationRequestDto)

        then: "request is created and is in the repository"
        clarificationRequestRepository.count() == 1L
        def result = clarificationRequestRepository.findAll().get(0)
        result.getId() != null
        result.getCreator().getId() == student.getId()
        result.getQuestion().getId() == question.getId()
        result.getCreationDate() != null
        result.getStatus() == ClarificationRequest.RequestStatus.PRIVATE
        and: "the clarification request was added to the student"
        def user = userRepository.findAll().get(0)
        user.getClarificationRequests().size() == 1
    }

    def "same student submits 2 requests for the same question"() {
        //throw exception
        given: "a second clarification request dto"
        clarificationRequestDto.setContent(CONTENT)
        def clarificationDto2 = new ClarificationRequestDto()
        clarificationDto2.setContent(CONTENT)

        when:
        clarificationService.submitClarificationRequest(questionId, student, clarificationRequestDto)
        clarificationService.submitClarificationRequest(questionId, student, clarificationDto2)

        then: "only the first one is saved and exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.DUPLICATE_CLARIFICATION_REQUEST
        clarificationRequestRepository.count() == 1L
        and: "the second clarification request wasn't added to the student"
        def user = userRepository.findAll().get(0)
        user.getClarificationRequests().size() == 1
    }


    @Unroll("invalid arguments: #content | #is_student | #has_answered || #error_message")
    def "invalid arguments"() {
        when:
        User user = student
        if (!has_answered) {
            user = student2
        } else if (!is_student) {
            user = teacher
        }

        int qId = questionId
        if (!is_question) {
            qId = INEXISTENT_QUESTION_ID
        }
        clarificationRequestDto.setContent(content)
        clarificationService.submitClarificationRequest(qId, user, clarificationRequestDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == error_message
        clarificationRequestRepository.count() == 0L
        and: "the clarification request wasn't added to the student"
        user.getClarificationRequests().size() == 0

        where:
        content | is_student | is_question | has_answered || error_message
        ""      | true       | true        | true         || ErrorMessage.CLARIFICATION_REQUEST_MISSING_CONTENT
        "    "  | true       | true        | true         || ErrorMessage.CLARIFICATION_REQUEST_MISSING_CONTENT
        null    | true       | true        | true         || ErrorMessage.CLARIFICATION_REQUEST_MISSING_CONTENT
        CONTENT | false      | true        | true         || ErrorMessage.ACCESS_DENIED
        CONTENT | true       | false       | true         || ErrorMessage.QUESTION_NOT_FOUND
        CONTENT | true       | true        | false        || ErrorMessage.QUESTION_NOT_ANSWERED_BY_STUDENT
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService()
        }
    }
}