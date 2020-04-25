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

@DataJpaTest
class ChangeClarificationRequestStatusServiceSpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"
    static final String CONTENT = "Test Content"
    static final int NONEXISTENT_ID = 5000

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
    ClarificationRequestDto clarificationRequestDto
    int studentId
    int questionId
    int teacherId
    
    def setup() {
        course = createCourse(COURSE_NAME)
        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        quiz = createQuiz(1, courseExecution, Quiz.QuizType.GENERATED)
        question = createQuestion(1, course)
        quizQuestion = new QuizQuestion(quiz, question, 1)
        student = createStudent(new User(), 1, 'NAME', 'USERNAME_ONE', courseExecution)
        teacher = createTeacher(2, "TEACHER", courseExecution)
        quizAnswer = new QuizAnswer(student, quiz)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        quizRepository.save(quiz)
        questionRepository.save(question)
        quizQuestionRepository.save(quizQuestion)
        userRepository.save(student)
        userRepository.save(teacher)
        quizAnswerRepository.save(quizAnswer)
        questionId = question.getId()
        studentId = student.getId()
        teacherId = teacher.getId()
    }

    private User createStudent(User student, int key, String name, String username, CourseExecution courseExecution) {
        student.setKey(key)
        student.setName(name)
        student.setUsername(username)
        student.setRole(User.Role.STUDENT)
        student.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(student)
        return student
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
        course.addQuestion(question)
        return question
    }

    private Quiz createQuiz(int key, CourseExecution courseExecution, Quiz.QuizType type) {
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

    private Course createCourse(String name) {
        def course = new Course()
        course.setName(name)
        return course
    }

    def "make submitted and answered clarification request public"() {
        given:
        clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setContent(CONTENT)
        clarificationRequestDto = clarificationService.submitClarificationRequest(questionId, studentId, clarificationRequestDto)
        clarificationService.submitClarificationRequestAnswer(teacher, clarificationRequestDto.getId(), CONTENT)

        when:
        def result = clarificationService.changeClarificationRequestStatus(clarificationRequestDto.getId(), true)

        then:
        result != null
        result.getContent() == CONTENT
        result.getOwner() == studentId
        result.getQuestionId() == questionId
        result.getStatus() == ClarificationRequest.RequestStatus.PUBLIC
    }


    def "make submitted and answered clarification request public and private again"() {
        given:
        clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setContent(CONTENT)
        clarificationRequestDto = clarificationService.submitClarificationRequest(questionId, studentId, clarificationRequestDto)
        clarificationService.submitClarificationRequestAnswer(teacher, clarificationRequestDto.getId(), CONTENT)

        when:
        clarificationService.changeClarificationRequestStatus(clarificationRequestDto.getId(), true)
        def result = clarificationService.changeClarificationRequestStatus(clarificationRequestDto.getId(), false)

        then:
        result != null
        result.getContent() == CONTENT
        result.getOwner() == studentId
        result.getQuestionId() == questionId
        result.getStatus() == ClarificationRequest.RequestStatus.PRIVATE
    }

    def "clarification request wasn't answered"() {
        given:
        clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setContent(CONTENT)
        clarificationRequestDto = clarificationService.submitClarificationRequest(questionId, studentId, clarificationRequestDto)

        when:
        clarificationService.changeClarificationRequestStatus(clarificationRequestDto.getId(), true)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CLARIFICATION_REQUEST_UNANSWERED
    }




    def "clarification request doesn't exist"() {
        when:
        clarificationService.deleteClarificationRequest(studentId, NONEXISTENT_ID)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CLARIFICATION_REQUEST_NOT_FOUND
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService();
        }
    }
}
