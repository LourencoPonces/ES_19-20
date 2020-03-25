
package pt.ulisboa.tecnico.socialsoftware.tutor.studentquestion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.CheckStudentQuestionStatusService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime

@DataJpaTest
class CheckStudentQuestionStatusProfilingTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"

    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    public static final String STUDENT_USERNAME = "ist199999"

    public static final Integer STUDENT_KEY = 1

    public static final String OPTION_CONTENT = "optionId content"
    public static final String TOPIC_NAME = "topic name"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'

    @Autowired
    CheckStudentQuestionStatusService studentCheckQuestionStatusService


    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    UserRepository userRepository

    def student
    def course
    def courseExecution

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        // course execution
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        // student
        student = createUser(courseExecution, User.Role.STUDENT, STUDENT_USERNAME, STUDENT_KEY)
        userRepository.save(student)
    }

    private StudentQuestion createStudentQuestion(User user, Course course, Integer key) {
        def studentQuestion = new StudentQuestionDTO()
        studentQuestion.setTitle(QUESTION_TITLE)
        studentQuestion.setContent(QUESTION_CONTENT)
        studentQuestion.setCreationDate(LocalDateTime.now().format(Course.formatter));
        studentQuestion.setStudentQuestionKey(key)
        setTopics(studentQuestion)
        setOptions(studentQuestion)
        return new StudentQuestion(course, studentQuestion, user)
    }
    

    private void setOptions(StudentQuestionDTO studentQuestion) {
        def option = new OptionDto()
        option.setContent(OPTION_CONTENT)
        option.setCorrect(true)
        def optionList = new ArrayList<OptionDto>()
        optionList.add(option)
        studentQuestion.setOptions(optionList)
    }

    private void setTopics(StudentQuestionDTO studentQuestion) {
        def topic = new TopicDto()
        topic.setName(TOPIC_NAME)
        def topicList = new ArrayList<TopicDto>()
        topicList.add(topic)
        studentQuestion.setTopics(topicList)
    }


    private User createUser(CourseExecution courseExecution, User.Role role, String username, Integer key) {
        def user = new User()
        user.setUsername(username)
        user.setKey(key)
        user.getCourseExecutions().add(courseExecution)
        user.setRole(role)
        return user
    }

    def evaluateQuestion(isAccepted, isRejected, question) {
        if(isAccepted)
            question.setSubmittedStatus(StudentQuestion.SubmittedStatus.APPROVED)
        else if(isRejected)
            question.setSubmittedStatus(StudentQuestion.SubmittedStatus.REJECTED)
        else
            question.setSubmittedStatus(StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL)
    }

    /* ===========================================
     * F3: Student checks his questions
     * ===========================================
     */

    def "performace testing to evaluate 10000 students checking 300 questions"() {
        def limit = 1 //USE 10000
        def limitQuestions = 1 //USE 300
        given: '10000 studentQuestions '
        StudentQuestion studentQuestion
        1.upto(limitQuestions, {
            studentQuestion = createStudentQuestion(student, course, it)
            if(it%1 == 1) {
                evaluateQuestion(true, false, studentQuestion)
            } else if (it%3 == 2) {
                evaluateQuestion(false, true, studentQuestion)
            }
            studentQuestionRepository.save(studentQuestion)
        })

        when:
        1.upto(limit, {
            studentCheckQuestionStatusService.getAllStudentQuestion(student.getId())
        })

        then:
        true

    }

    @TestConfiguration
    static class StudentCheckQuestionImplTestContextConfiguration {

        @Bean
        CheckStudentQuestionStatusService studentCheckQuestionStatusService() {
            return new CheckStudentQuestionStatusService();
        }
    }
}