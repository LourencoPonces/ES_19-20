package pt.ulisboa.tecnico.socialsoftware.tutor.studentquestion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TeacherEvaluatesStudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class TeacherApprovesStudentQuestionTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    @Autowired
    TeacherEvaluatesStudentQuestionService teacherEvaluatesStudentQuestionService

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    UserRepository userRepository

    def course
    def courseExecution
    def user

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        user = new User()
        user.setKey(1)
        user.setUsername("ist199999")
        user.getCourseExecutions().add(courseExecution)
        userRepository.save(user)
    }

    def "approve existing pending question with no justification"() {
        given: 'a studentQuestion'
        def studentQuestionDTO = new StudentQuestionDTO()
        studentQuestionDTO.setUser(user.getUsername())
        def studentQuestion = new StudentQuestion(course, studentQuestionDTO, user)
        studentQuestionRepository.save(studentQuestion)

        when:
        teacherEvaluatesStudentQuestionService.acceptStudentQuestion(studentQuestion)

        then:
        studentQuestion.getSubmittedStatus() == StudentQuestion.SubmittedStatus.APPROVED
    }

    def "approve existing pending question with justification"() {
        given: 'a studentQuestion'
        def studentQuestionDTO = new StudentQuestionDTO(user)
        def studentQuestion = new StudentQuestion(course, studentQuestionDTO, user)
        studentQuestionRepository.save(studentQuestion)

        def justification = "Very good question"

        when:
        teacherEvaluatesStudentQuestionService.acceptStudentQuestion(studentQuestion, justification)

        then:
        studentQuestion.getSubmittedStatus() == StudentQuestion.SubmittedStatus.APPROVED
        studentQuestion.getJustification() == justification
    }

    def "approve already evaluated student question, #evaluation->#finalStatus"() {
        given: 'a studentQuestion'
        def studentQuestionDTO = new StudentQuestionDTO(user)
        studentQuestionDTO.setSubmittedStatus(StudentQuestion.SubmittedStatus.evaluation)

        def studentQuestion = new StudentQuestion(course, studentQuestionDTO, user)
        studentQuestionRepository.save(studentQuestion)

        def justification = "Very good question"

        when:
        teacherEvaluatesStudentQuestionService.acceptStudentQuestion(studentQuestion, justification)

        then:
        studentQuestion.getSubmittedStatus() == StudentQuestion.SubmittedStatus.finalStatuss
        studentQuestion.getJustification() == justification

        where:
        evaluation || finalStatus
        APPROVED || APPROVED
        REJECTED || APPROVED
    }

    @TestConfiguration
    static class TeacherEvaluatesImplTestContextConfiguration {

        @Bean
        TeacherEvaluatesStudentQuestionService teacherEvaluatesStudentQuestionService1() {
            return  new TeacherEvaluatesStudentQuestionService();
        }
    }
}
