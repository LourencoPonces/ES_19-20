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

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_REJECT_WITHOUT_JUSTIFICATION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_EVALUATE_PROMOTED_QUESTION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_JUSTIFICATION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_REJECT_ACCEPTED_SUGGESTION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_FOUND

@DataJpaTest
class TeacherRejectsStudentQuestionTest extends Specification {


    public static final String COURSE_NAME = "Software Architecture"

    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    public static final String USER_NAME = "ist199999"

    public static final String QUESTION_TITLE = "Question Title"
    public static final String QUESTION_CONTENT = "Question Content"

    public static final String OPTION_CONTENT = "Option Content"

    public static final Integer STUDENT_QUESTION_KEY = 1
    public static final Integer FAKE_STUDENT_QUESTION_ID = 2

    public static final String VALID_JUSTIFICATION = "irrelevant question"

    public static final String LONG_JUSTIFICATION = "very long long long long long long long long long long long " +
            "long long long long long long long long long long long long long long long long long long long long " +
            "long long long long long long long long long long long long long long long long long justification"

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
        studentQuestion.setTitle(QUESTION_TITLE)
        studentQuestion.setContent(QUESTION_CONTENT)
        studentQuestion.addTopic(new Topic())

        Option o = new Option()
        o.setCorrect(true)
        o.setQuestion(studentQuestion)
        o.setContent(OPTION_CONTENT)
        o.setSequence(0)
        studentQuestion.addOption(o)
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


    def "reject student question with valid justification"() {
        when:
        teacherEvaluatesStudentQuestionService.evaluateStudentQuestion(savedQuestionId, StudentQuestion.SubmittedStatus.REJECTED, VALID_JUSTIFICATION)

        then:
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getSubmittedStatus() == StudentQuestion.SubmittedStatus.REJECTED
        result.getJustification() == VALID_JUSTIFICATION
    }

    // impossible to reject question with no justification parameter
    def "reject student question with invalid justification"() {
        when:
        teacherEvaluatesStudentQuestionService.evaluateStudentQuestion(savedQuestionId, StudentQuestion.SubmittedStatus.REJECTED, justification)

        then:
        def error = thrown(TutorException)
        error.errorMessage == result


        // invalid justifications:
        // * empty strings or null
        where:
        justification || result
        ""            || INVALID_JUSTIFICATION
        "   "         || INVALID_JUSTIFICATION
        "\n  \t"      || INVALID_JUSTIFICATION
        LONG_JUSTIFICATION  || INVALID_JUSTIFICATION
        null          || CANNOT_REJECT_WITHOUT_JUSTIFICATION
    }

    def "reject already accepted student question"() {
        given: 'pending student question'
        studentQuestionRepository.count() == 1L
        def question = studentQuestionRepository.findAll().get(0)
        evaluateQuestion(true, question)


        when:
        teacherEvaluatesStudentQuestionService.evaluateStudentQuestion(savedQuestionId, StudentQuestion.SubmittedStatus.REJECTED, VALID_JUSTIFICATION)

        then:
        studentQuestionRepository.findAll().get(0).getSubmittedStatus() == StudentQuestion.SubmittedStatus.REJECTED
    }

    def "reject already rejected student question"() {
        given: 'pending student question'
        studentQuestionRepository.count() == 1L
        def question = studentQuestionRepository.findAll().get(0)
        evaluateQuestion(false, question)


        when:
        teacherEvaluatesStudentQuestionService.evaluateStudentQuestion(savedQuestionId, StudentQuestion.SubmittedStatus.REJECTED, VALID_JUSTIFICATION)

        then:
        studentQuestionRepository.findAll().get(0).getSubmittedStatus() == StudentQuestion.SubmittedStatus.REJECTED
    }

    def "reject already promoted student question"() {
        given: 'pending student question'
        studentQuestionRepository.count() == 1L
        def question = studentQuestionRepository.findAll().get(0)
        question.setSubmittedStatus(StudentQuestion.SubmittedStatus.PROMOTED)


        when:
        teacherEvaluatesStudentQuestionService.evaluateStudentQuestion(savedQuestionId, StudentQuestion.SubmittedStatus.REJECTED, null)

        then:
        def error = thrown(TutorException)
        error.errorMessage == CANNOT_EVALUATE_PROMOTED_QUESTION
    }

    def "reject non existing student question"() {
        when:
        teacherEvaluatesStudentQuestionService.evaluateStudentQuestion(FAKE_STUDENT_QUESTION_ID, StudentQuestion.SubmittedStatus.REJECTED, VALID_JUSTIFICATION)

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
