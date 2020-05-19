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
class GetStudentClarificationRequestsServiceSpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"
    static final String CONTENT = "Test Content"
    static final String CONTENT_2 = "Test Content 2"

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
    ClarificationService clarificationService

    Course course
    CourseExecution courseExecution
    Question question
    Quiz quiz
    QuizQuestion quizQuestion
    QuizAnswer quizAnswer
    User student, student2

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        quiz = createQuiz(1, courseExecution, "GENERATED")
        question = createQuestion(1, course)
        quizQuestion = new QuizQuestion(quiz, question, 1)
        student = createStudent(1, 'NAME', 'USERNAME_ONE', courseExecution)
        student2 = createStudent(2, 'NAME2', 'USERNAME_TWO', courseExecution)
        quizAnswer = new QuizAnswer(student, quiz)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        quizRepository.save(quiz)
        questionRepository.save(question)
        quizQuestionRepository.save(quizQuestion)
        userRepository.save(student)
        userRepository.save(student2)
        quizAnswerRepository.save(quizAnswer)
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

    def "student submitted 1 clarification request"() {
        given:
        def clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setContent(CONTENT)
        clarificationRequestDto.setType(ClarificationRequest.RequestType.MULTIPLE_CORRECT)
        clarificationService.submitClarificationRequest(question.id, student.id, clarificationRequestDto)

        when:
        def result = clarificationService.getStudentClarificationRequests(student.id)
        def requests = result.requests

        then:
        result != null
        result.requests != null
        requests.size() == 1
        ClarificationRequestDto req = requests[0]
        req.creatorUsername == student.username
        req.content == CONTENT
        req.questionId == question.id
        req.type == ClarificationRequest.RequestType.MULTIPLE_CORRECT
    }

    def "student submitted 2 clarification requests"() {
        given: "a clarification request for one question"
        def clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setContent(CONTENT)
        clarificationService.submitClarificationRequest(question.id, student.id, clarificationRequestDto)

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
        def cr = new ClarificationRequestDto()
        cr.setContent(CONTENT_2)
        clarificationService.submitClarificationRequest(q.getId(), student.id, cr)

        when:
        def result = clarificationService.getStudentClarificationRequests(student.id)
        List<ClarificationRequestDto> requests = result.requests
        requests.sort(Comparator.comparing { r -> ((ClarificationRequestDto) r).id })

        then: "returns list ordered by request Id DSC"
        requests.size() == 2
        requests[0].creatorUsername == student.username
        requests[0].content == CONTENT
        requests[0].questionId == question.id
        requests[1].creatorUsername == student.username
        requests[1].content == CONTENT_2
        requests[1].questionId == q.id

        and: "user information is returned"
        result.names.get(student.username) == student.name
    }

    def "student didn't submit clarification requests"() {
        when:
        def result = clarificationService.getStudentClarificationRequests(student.id)

        then:
        result != null
        result.requests != null
        result.requests.isEmpty()
        result.names.isEmpty()
    }

    def "student didn't submit any clarification requests but others exist"() {
        when: "another student submits a request"
        def clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setContent(CONTENT)
        clarificationService.submitClarificationRequest(question.id, student.id, clarificationRequestDto)
        def result = clarificationService.getStudentClarificationRequests(student2.id)

        then:
        result != null
        result.requests != null
        result.requests.isEmpty()
        result.names.isEmpty()
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService()
        }
    }
}
