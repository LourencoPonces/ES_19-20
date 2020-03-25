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

import java.util.List

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_JUSTIFICATION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_REJECT_ACCEPTED_SUGGESTION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_FOUND

@DataJpaTest
class TeacherEvaluatesStudentQuestionProfilingTest extends Specification {


    public static final String COURSE_NAME = "Software Architecture"

    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    public static final String USER_NAME = "ist199999"

    public static final Integer STUDENT_QUESTION_KEY = 1
    public static final Integer FAKE_STUDENT_QUESTION_ID = 2

    public static final String VALID_JUSTIFICATION = "irrelevant question"

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
    User user
    Course course

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        // course execution
        def courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        // user
        user = createUser(courseExecution, User.Role.TEACHER)
        userRepository.save(user)
    }

    private StudentQuestion createStudentQuestion(User user, Course course, Integer key) {
        def studentQuestion = new StudentQuestion()
        studentQuestion.addTopic(new Topic())

        Option o = new Option()
        o.setCorrect(true)
        o.setQuestion(studentQuestion)
        studentQuestion.addOption(o)
        studentQuestion.setKey(key)
        studentQuestion.setStudentQuestionKey(key)
        studentQuestion.setUser(user)
        studentQuestion.setCourse(course)
        studentQuestion
    }

    private User createUser(CourseExecution courseExecution, User.Role role) {
        def user = new User()
        user.setKey(1)
        user.setUsername(USER_NAME)
        user.getCourseExecutions().add(courseExecution)
        user.setRole(role)
        user
    }


    /* ===========================================
     * F2: Teacher evaluates student question
     * ===========================================
     */
    def "performance testing to evaluate 10000 student questions"() {
        def limit = 10000
        List<Integer> studentQuestions =new LinkedList<Integer>();

        Random random = new Random()

        given: "10000 student questions"
        1.upto(limit, {
            // studentQuestion
            StudentQuestion studentQuestion = createStudentQuestion(user, course, it)
            studentQuestionRepository.save(studentQuestion)

            // save studentQuestionId
            studentQuestions.add(studentQuestion.getId())
        })

        when:
        for(Integer i : studentQuestions) {
            if(random.nextBoolean()) {
                teacherEvaluatesStudentQuestionService.acceptStudentQuestion(i, "")
            } else {
                teacherEvaluatesStudentQuestionService.rejectStudentQuestion(i, "Bad question >:c")
            }
        }

        then:
        true
    }


    @TestConfiguration
    static class TeacherEvaluatesImplTestContextConfiguration {

        @Bean
        TeacherEvaluatesStudentQuestionService teacherEvaluatesStudentQuestionService() {
            return  new TeacherEvaluatesStudentQuestionService();
        }
    }
}
