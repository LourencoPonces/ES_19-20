
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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.CheckStudentQuestionStatusService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_FOUND

@DataJpaTest
class CheckStudentQuestionStatusTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"

    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    public static final String USER_NAME = "ist199999"

    public static final Integer STUDENT_QUESTION_KEY = 1
    public static final Integer FAKE_STUDENT_QUESTION_ID = 2

    @Autowired
    CheckStudentQuestionStatusService studentCheckQuestionStatusService


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

    def "teacher checks suggestion"() {
        when:
        studentCheckQuestionStatusService.acceptStudentQuestion(FAKE_STUDENT_QUESTION_ID)

        then:
        def error = thrown(TutorException)
        error.errorMessage == STUDENT_QUESTION_NOT_FOUND
    }

    def "check status of existing suggestions (accepted, rejected, pending)"() {
        // return question status
        expect: false
    }

    def "check status of non existing suggestions"() {
        // empty collection
        expect: false
    }

    def evaluateQuestion(isAccepted, isRejected, question) {
        if(isAccepted)
            question.setSubmittedStatus(StudentQuestion.SubmittedStatus.APPROVED)
        else if(isRejected)
            question.setSubmittedStatus(StudentQuestion.SubmittedStatus.REJECTED)
        else
            question.setSubmittedStatus(StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL)
    }



    @TestConfiguration
    static class StudentCheckQuestionImplTestContextConfiguration {

        @Bean
        CheckStudentQuestionStatusService studentCheckQuestionStatusService() {
            return new CheckStudentQuestionStatusService();
        }
    }
}