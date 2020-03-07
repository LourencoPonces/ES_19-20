package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service


import spock.lang.Specification
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User


import java.time.format.DateTimeFormatter

@DataJpaTest
class SubmitClarificationRequestServiceSpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"
    static final String CONTENT = "This is a test request."
    static final Role ROLE = Role.STUDENT
    static final String USERNAME_ONE = "STUDENT_ONE"
    static final String USERNAME_TWO = "STUDENT_TWO"
    static final String NAME = "NAME"
    static final int WRONG_QUESTION_ID = 100

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

    def course
    def courseExecution
    def question
    def quiz
    def quizQuestion
    def quizAnswer
    def student
    def userDto
    def clarificationRequestDto
    def formatter
    def questionDto


    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        course = new Course()
        course.setName(COURSE_NAME)

        courseExecution = new CourseExecution()
        courseExecution.setCourse(course)
        courseExecution.setAcronym(ACRONYM)
        courseExecution.setAcademicTerm(ACADEMIC_TERM)

        // quiz
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setType(Quiz.QuizType.GENERATED)
        quiz.setCourseExecution(courseExecution)
        courseExecution.addQuiz(quiz)


        // question
        question = new Question()
        question.setKey(1)
        question.setCourse(course)
        course.addQuestion(question)

        // quiz question
        quizQuestion = new QuizQuestion(quiz, question, 1)


        // student
        student = new User()
        student.setKey(1)
        student.setName(NAME)
        student.setUsername(USERNAME_ONE)
        student.setRole(ROLE)
        student.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(student)

        // quiz answer
        quizAnswer = new QuizAnswer(student, quiz)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        quizRepository.save(quiz)
        questionRepository.save(question)
        quizQuestionRepository.save(quizQuestion)
        userRepository.save(student)
        quizAnswerRepository.save(quizAnswer)
    }

    def "the question has been answered and submit request"() {
        //the clarification request is created
        given: "a clarification request dto"
        clarificationRequestDto = new ClarificationRequestDto()

        when:
        int questionId = question.getId()
        int studentId = student.getId()
        clarificationRequestDto = clarificationService.submitClarificationRequest(CONTENT, questionId, studentId, clarificationRequestDto)

        then:"request is created and is in the repository"
        clarificationRequestRepository.count() == 1L
        def result = clarificationRequestRepository.findAll().get(0)
        result.getId() != null
        result.getOwner().getId() == student.getId()
        result.getQuestion().getId() == question.getId()
        result.getCreationDate() != null
        and: "the clarification request was added to the student"
        def user = userRepository.findAll().get(0)
        user.getClarificationRequests().size() == 1
    }

    def "same student submits 2 requests for the same question"() {
        //throw exception
        given: "a first clarification request dto"
        clarificationRequestDto = new ClarificationRequestDto()
        and: "a second clarification request dto"
        def clarificationDto2 = new ClarificationRequestDto()

        when:
        int questionId = question.getId()
        int studentId = student.getId()
        clarificationService.submitClarificationRequest(CONTENT, questionId, studentId, clarificationRequestDto)
        clarificationService.submitClarificationRequest(CONTENT, questionId, studentId, clarificationDto2)

        then: "only the first one is saved and exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.DUPLICATE_CLARIFICATION_REQUEST
        clarificationRequestRepository.count() == 1L
        and: "the clarification request wasn't added to the student"
        def user = userRepository.findAll().get(0)
        user.getClarificationRequests().size() == 1
    }

    def "the question has not been answered"() {
        //throw exception
        given: "a user that didn't answer the question"
        def student2 = new User()
        student2.setKey(2)
        student2.setName(NAME)
        student2.setUsername(USERNAME_TWO)
        student2.setRole(ROLE)
        userRepository.save(student2)

        when:
        int questionId = question.getId()
        int student2Id = student2.getId()
        clarificationService.submitClarificationRequest(CONTENT, questionId, student2Id, clarificationRequestDto)

        then: "can't create request"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.QUESTION_NOT_ANSWERED_BY_STUDENT
        clarificationRequestRepository.count() == 0L
        and: "the clarification request wasn't added to the student"
        def user = userRepository.findAll().get(0)
        user.getClarificationRequests().size() == 0
    }

    def "the question doesn't exist"() {
        given:"a clarification request dto"
        clarificationRequestDto = new ClarificationRequestDto()

        when:
        int studentId = student.getId()
        clarificationService.submitClarificationRequest(CONTENT, WRONG_QUESTION_ID, studentId, clarificationRequestDto)

        then:
        def exception = thrown(TutorException)
            exception.getErrorMessage() == ErrorMessage.QUESTION_NOT_FOUND
        clarificationRequestRepository.count() == 0L
        and: "the clarification request wasn't added to the student"
        def user = userRepository.findAll().get(0)
        user.getClarificationRequests().size() == 0
    }

    def "the user isn't a student"() {
        given:"a clarification request dto"
        clarificationRequestDto = new ClarificationRequestDto()

        when:
        int questionId = question.getId()
        student.setRole(User.Role.TEACHER)
        int userId = student.getId()
        clarificationService.submitClarificationRequest(CONTENT, questionId, userId, clarificationRequestDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.ACCESS_DENIED
        clarificationRequestRepository.count() == 0L
        and: "the clarification request wasn't added to the user"
        def user = userRepository.findAll().get(0)
        user.getClarificationRequests().size() == 0
    }

    def "content is empty"() {
        //throw exception
        given: "a clarification request dto"
        clarificationRequestDto = new ClarificationRequestDto()

        when: "no content is provided"
        int questionId = question.getId()
        int studentId = student.getId()
        clarificationService.submitClarificationRequest(null,questionId, studentId, clarificationRequestDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CLARIFICATION_REQUEST_MISSING_CONTENT
        clarificationRequestRepository.count() == 0L
        and: "the clarification request wasn't added to the student"
        def user = userRepository.findAll().get(0)
        user.getClarificationRequests().size() == 0
    }

    def "content is blank"() {
        //throw exception
        given: "a clarification request dto"
        clarificationRequestDto = new ClarificationRequestDto()

        when: "a blank string is provided"
        int questionId = question.getId()
        int studentId = student.getId()
        clarificationService.submitClarificationRequest("  ", questionId, studentId, clarificationRequestDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CLARIFICATION_REQUEST_MISSING_CONTENT
        clarificationRequestRepository.count() == 0L
        and: "the clarification request wasn't added to the student"
        def user = userRepository.findAll().get(0)
        user.getClarificationRequests().size() == 0
    }


    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService();
        }
    }
}