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
class getStudentClarificationRequestsServiceSpockTest extends Specification {
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
    ClarificationRequestDto clarificationRequestDto
    int studentId
    int questionId


    def setup() {
        course = createCourse(COURSE_NAME)
        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        quiz = createQuiz(1, courseExecution, Quiz.QuizType.GENERATED)
        question = createQuestion(1, course)
        quizQuestion = new QuizQuestion(quiz, question, 1)
        student = createStudent(new User(), 1, 'NAME', 'USERNAME_ONE', courseExecution)
        quizAnswer = new QuizAnswer(student, quiz)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        quizRepository.save(quiz)
        questionRepository.save(question)
        quizQuestionRepository.save(quizQuestion)
        userRepository.save(student)
        quizAnswerRepository.save(quizAnswer)
        questionId = question.getId()
        studentId = student.getId()
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

    def "student submitted 1 clarification request"() {
        given:
        clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setContent("some request")
        clarificationService.submitClarificationRequest(questionId, studentId, clarificationRequestDto)

        when:
        def result = clarificationService.getStudentClarificationRequests(studentId)

        then:
        result != null
        result.size() == 1
        ClarificationRequestDto req = result[0]
        req.owner == studentId
        req.content == "some request"
        req.questionId == question.getId()
    }

    def "student submitted 2 clarification requests"() {
        given: "a clarification request for one question"
        clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setContent("some request")
        clarificationService.submitClarificationRequest(questionId, studentId, clarificationRequestDto)

        and: "another answered question"
        def q = createQuestion(2, course)
        def qq = new QuizQuestion(quiz, q, 2)
        def qa = new QuizAnswer(student, quiz)
        quizRepository.save(quiz)
        questionRepository.save(q)
        quizQuestionRepository.save(qq)
        quizAnswerRepository.save(qa)
        userRepository.save(student)

        and: "a clarification request to this question"
        def cr = new ClarificationRequestDto();
        cr.setContent("another request")
        clarificationService.submitClarificationRequest(q.getId(), studentId, cr)

        when:
        def result = clarificationService.getStudentClarificationRequests(studentId)

        then:
        result != null
        result.size() == 2
        ClarificationRequestDto req1 = result[0]
        req1.owner == studentId
        req1.content == "some request"
        req1.questionId == question.getId()
        ClarificationRequestDto req2 = result[1]
        req2.owner == studentId
        req2.content == "another request"
        req2.questionId == q.getId()
    }

    def "student didn't submit clarification requests"() {
        when:
        def result = clarificationService.getStudentClarificationRequests(studentId)

        then:
        result != null
        result.size() == 0
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService();
        }
    }
}
