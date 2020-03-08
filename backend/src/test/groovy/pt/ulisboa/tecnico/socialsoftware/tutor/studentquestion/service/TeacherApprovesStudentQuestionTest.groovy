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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class TeacherApprovesStudentQuestionTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String OPTION_CONTENT = "optionId content"
    public static final String TOPIC_NAME = "topic name"

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


    def setup() {
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        // course execution
        def courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        // user
        def user = new User()
        user.setKey(1)
        user.setUsername("ist199999")
        user.getCourseExecutions().add(courseExecution)
        userRepository.save(user)

        // options
        def option = new Option()
        option.setContent(OPTION_CONTENT)
        option.setCorrect(true);

        // topic
        def topic = new Topic()

        // studentQuestion
        def studentQuestion = new StudentQuestion()
        studentQuestion.addTopic(topic)
        studentQuestion.addOption(option)

        // save studentQuestion
        studentQuestionRepository.save(studentQuestion)
    }


    def "approve existing pending question with no justification"() {
        given: 'pending student question'
        studentQuestionRepository.
//        given: 'a studentQuestion'
//        def studentQuestionDTO = new StudentQuestionDTO()
//        studentQuestionDTO.setUser(user.getUsername())
//        def studentQuestion = new StudentQuestion(course, studentQuestionDTO, user)
//        studentQuestionRepository.save(studentQuestion)
//
//        when:
//        teacherEvaluatesStudentQuestionService.acceptStudentQuestion(studentQuestion)
//
//        then:
//        studentQuestion.getSubmittedStatus() == StudentQuestion.SubmittedStatus.APPROVED
    }

    def "approve existing pending question with justification"() {
//        given: 'a studentQuestion'
//        def studentQuestionDTO = new StudentQuestionDTO(user)
//        def studentQuestion = new StudentQuestion(course, studentQuestionDTO, user)
//        studentQuestionRepository.save(studentQuestion)
//
//        def justification = "Very good question"
//
//        when:
//        teacherEvaluatesStudentQuestionService.acceptStudentQuestion(studentQuestion, justification)
//
//        then:
//        studentQuestion.getSubmittedStatus() == StudentQuestion.SubmittedStatus.APPROVED
//        studentQuestion.getJustification() == justification
    }

    def "approve already evaluated student question, #evaluation->#finalStatus"() {
//        given: 'a studentQuestion'
//        def studentQuestionDTO = new StudentQuestionDTO(user)
//        studentQuestionDTO.setSubmittedStatus(StudentQuestion.SubmittedStatus.evaluation)
//
//        def studentQuestion = new StudentQuestion(course, studentQuestionDTO, user)
//        studentQuestionRepository.save(studentQuestion)
//
//        def justification = "Very good question"
//
//        when:
//        teacherEvaluatesStudentQuestionService.acceptStudentQuestion(studentQuestion, justification)
//
//        then:
//        studentQuestion.getSubmittedStatus() == StudentQuestion.SubmittedStatus.finalStatuss
//        studentQuestion.getJustification() == justification
//
//        where:
//        evaluation || finalStatus
//        APPROVED || APPROVED
//        REJECTED || APPROVED
    }

    @TestConfiguration
    static class TeacherEvaluatesImplTestContextConfiguration {

        @Bean
        TeacherEvaluatesStudentQuestionService teacherEvaluatesStudentQuestionService() {
            return  new TeacherEvaluatesStudentQuestionService();
        }
    }
}
