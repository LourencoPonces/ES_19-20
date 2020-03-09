package pt.ulisboa.tecnico.socialsoftware.tutor.studentquestion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TeacherEvaluatesStudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_JUSTIFICATION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_FOUND

@DataJpaTest
class TeacherApprovesStudentQuestionTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"

    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    public static final String USER_NAME = "ist199999"

    public static final Integer STUDENT_QUESTION_KEY = 1
    public static final Integer FAKE_STUDENT_QUESTION_ID = 2

    public static final String JUSTIFICATION = "very good question"

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

    def savedQuestionId

    def setup() {
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        // course execution
        def courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        // user
        User user = createUser(courseExecution)
        userRepository.save(user)

        // studentQuestion
        StudentQuestion studentQuestion = createStudentQuestion(user, course)
        studentQuestionRepository.save(studentQuestion)

        // get studentQuestionId
        savedQuestionId = studentQuestion.getId()
    }

    private StudentQuestion createStudentQuestion(User user, Course course) {
        def studentQuestion = new StudentQuestion()
        studentQuestion.addTopic(new Topic())
        studentQuestion.addOption(new Option())
        studentQuestion.setKey(STUDENT_QUESTION_KEY)
        studentQuestion.setStudentQuestionKey(STUDENT_QUESTION_KEY)
        studentQuestion.setUser(user)
        studentQuestion.setCourse(course)
        studentQuestion
    }

    private User createUser(CourseExecution courseExecution) {
        def user = new User()
        user.setKey(1)
        user.setUsername(USER_NAME)
        user.getCourseExecutions().add(courseExecution)
        user
    }



    def "approve existing pending question with no justification"() {
        when:
        teacherEvaluatesStudentQuestionService.acceptStudentQuestion(savedQuestionId)

        then:
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getSubmittedStatus() == StudentQuestion.SubmittedStatus.APPROVED
        result.getJustification() == ""
    }

    def "approve existing pending question with valid justification"() {
        when:
        teacherEvaluatesStudentQuestionService.acceptStudentQuestion(savedQuestionId, JUSTIFICATION)

        then:
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getSubmittedStatus() == StudentQuestion.SubmittedStatus.APPROVED
        result.getJustification() == JUSTIFICATION
    }

    def "approve existing pending question with invalid justification"() {
        when:
        teacherEvaluatesStudentQuestionService.acceptStudentQuestion(savedQuestionId, justification)

        then:
        def error = thrown(TutorException)
        error.errorMessage == result

        // invalid justifications:
        //   empty strings or null
        where:
        justification || result
        ""            || INVALID_JUSTIFICATION
        "   "         || INVALID_JUSTIFICATION
        "\n  \t"      || INVALID_JUSTIFICATION
        null          || INVALID_JUSTIFICATION
    }

    def "approve already evaluated student question, #isApproved->#result"() {
        given: 'pending student question'
        studentQuestionRepository.count() == 1L
        def question = studentQuestionRepository.findAll().get(0)
        evaluateQuestion(isApproved, question)


        when:
        teacherEvaluatesStudentQuestionService.acceptStudentQuestion(savedQuestionId)

        then:
        studentQuestionRepository.findAll().get(0).getSubmittedStatus() == result

        where:
        isApproved || result
        true       || StudentQuestion.SubmittedStatus.APPROVED
        false      || StudentQuestion.SubmittedStatus.APPROVED
    }

    def "approve non existing student question"(){
        when:
        teacherEvaluatesStudentQuestionService.acceptStudentQuestion(FAKE_STUDENT_QUESTION_ID)

        then:
        def error = thrown(TutorException)
        error.errorMessage == STUDENT_QUESTION_NOT_FOUND
    }


    def evaluateQuestion(isAccepted, question) {
        question.setSubmittedStatus(
                isAccepted ?
                        StudentQuestion.SubmittedStatus.APPROVED
                        :
                        StudentQuestion.SubmittedStatus.REJECTED
        )
    }

    @TestConfiguration
    static class TeacherEvaluatesImplTestContextConfiguration {

        @Bean
        TeacherEvaluatesStudentQuestionService teacherEvaluatesStudentQuestionService() {
            return  new TeacherEvaluatesStudentQuestionService();
        }
    }
}
