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
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest
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


    def course
    def courseExecution
    def clarificationRequest
    def question
    def quiz
    def quizQuestion
    def quizAnswer
    def questionAnswer
    def student
    def formatter
    def studentId
    def questionId

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        course = createCourse(COURSE_NAME)
        courseRepository.save(course)

        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        courseExecutionRepository.save(courseExecution)

        student = createStudent(new User(), KEY_ONE, NAME, USERNAME_ONE, courseExecution)
        userRepository.save(student)

        1.upto(1000, {
            int i = it as int
            question = createQuestion(course)
            quiz = createQuiz(i, courseExecution, Quiz.QuizType.GENERATED)
            quizQuestion = new QuizQuestion(quiz, question, 1)
            quizAnswer = new QuizAnswer(student, quiz)
            questionAnswer = createQuestionAnswer(quizAnswer, quizQuestion)

            quizRepository.save(quiz)
            questionRepository.save(question)
            quizQuestionRepository.save(quizQuestion)
            quizAnswerRepository.save(quizAnswer)
            questionAnswerRepository.save(questionAnswer)

        })

        questionId = question.getId()
        studentId = student.getId()
    }

    private QuestionAnswer createQuestionAnswer(QuizAnswer quizAnswer, QuizQuestion quizQuestion) {
        questionAnswer = new QuestionAnswer()
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswer.setSequence(0)

        return questionAnswer
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

    private Question createQuestion(Course course) {
        question = new Question()
        question.setCourse(course)
        course.addQuestion(question)
        return question
    }

    private Quiz createQuiz(int i, CourseExecution courseExecution, Quiz.QuizType type) {
        quiz = new Quiz()
        quiz.setKey(i)
        quiz.setType(type)
        quiz.setCourseExecution(courseExecution)
        courseExecution.addQuiz(quiz)
        return quiz
    }

    private CourseExecution createCourseExecution(Course course, String acronym, String term) {
        courseExecution = new CourseExecution()
        courseExecution.setCourse(course)
        courseExecution.setAcronym(acronym)
        courseExecution.setAcademicTerm(term)
        return courseExecution
    }

    private Course createCourse(String name) {
        course = new Course()
        course.setName(name)
        return course
    }

    def "submit 1000 requests to 1000 different questions"() {
        when:
        1.upto(1000, {
            clarificationRequest = new ClarificationRequestDto()
            clarificationRequest.setContent(CONTENT)
            clarificationService.submitClarificationRequest(it as int, student.getId(), clarificationRequest)
        })

        then:
        true
    }


    @TestConfiguration
    static class ClarificationServicePerformanceTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService();
        }
    }
}