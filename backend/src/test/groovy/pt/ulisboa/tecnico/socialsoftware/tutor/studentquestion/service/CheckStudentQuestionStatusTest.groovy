
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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@DataJpaTest
class CheckStudentQuestionStatusTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"

    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    public static final String TEACHER_USERNAME = "ist911111"
    public static final String STUDENT_USERNAME = "ist199999"

    public static final Integer STUDENT_KEY = 1
    public static final Integer TEACHER_KEY = 2

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

    def savedQuestionId
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
        student = createUser(courseExecution, STUDENT_USERNAME, STUDENT_KEY)
        student.setRole(User.Role.STUDENT)
        userRepository.save(student)
    }

    private StudentQuestion createStudentQuestion(User user, Course course) {
        def studentQuestion = new StudentQuestionDTO()
        studentQuestion.setTitle(QUESTION_TITLE)
        studentQuestion.setContent(QUESTION_CONTENT)
        def topic =  new TopicDto()
        topic.setName(TOPIC_NAME)
        def topicList = new ArrayList<TopicDto>()
        topicList.add(topic)
        studentQuestion.setTopics(topicList)
        def option = new OptionDto()
        option.setContent(OPTION_CONTENT)
        option.setCorrect(true)
        def optionList = new ArrayList<OptionDto>()
        optionList.add(option)
        studentQuestion.setOptions(optionList)
        studentQuestion.setKey(questionRepository.getMaxQuestionNumber() == null ? 1 : questionRepository.getMaxQuestionNumber()+1 )
        studentQuestion.setStudentQuestionKey( studentQuestionRepository.getMaxQuestionNumber() == null ? 1 : studentQuestionRepository.getMaxQuestionNumber()+1)
        return new StudentQuestion(course, studentQuestion, user)
    }

    private User createUser(CourseExecution courseExecution, String username, Integer key) {
        def user = new User()
        user.setUsername(username)
        user.setKey(key)
        user.getCourseExecutions().add(courseExecution)
        return user
    }

    def "check status of non existing suggestions"() {
        given: 'no questions'

        when:
        List<StudentQuestionDTO> result = studentCheckQuestionStatusService.getAllStudentQuestion(student.getId())

        then:
        result.size() == 0;
    }

    def "teacher checks suggestion"() {
        given: 'a question'
        // studentQuestion
        StudentQuestion studentQuestion = createStudentQuestion(student, course)
        studentQuestionRepository.save(studentQuestion)

        // get studentQuestionId
        savedQuestionId = studentQuestion.getId()

        and: 'a teacher'
        def teacher = createUser(courseExecution, TEACHER_USERNAME, TEACHER_KEY)
        teacher.setRole(User.Role.TEACHER)
        userRepository.save(teacher)

        when:
        studentCheckQuestionStatusService.getAllStudentQuestion(teacher.getId())

        then:
        def error = thrown(TutorException)
        error.errorMessage == ACCESS_DENIED
    }

    def "check status of existing single suggestion (accepted, rejected, pending)"() {
        given: 'a submitted question'
        StudentQuestion studentQuestion = createStudentQuestion(student, course)
        evaluateQuestion(isAccepted, isRejected, studentQuestion)
        studentQuestionRepository.save(studentQuestion)

        when:
        def result = studentCheckQuestionStatusService.getAllStudentQuestion(student.getId())

        then:
        result.size() == 1
        result.get(0).getSubmittedStatus() == status

        where:
        isAccepted | isRejected || status
        false      | false      || StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL
        false      | true       || StudentQuestion.SubmittedStatus.REJECTED
        true       | false      || StudentQuestion.SubmittedStatus.APPROVED
    }

    def "check status of multiple suggestions"() {
        given: 'a pending question'
        def maxKey = 0

        StudentQuestion pendingQuestion = createStudentQuestion(student, course)
        studentQuestionRepository.save(pendingQuestion)

        and: 'an approved question'
        StudentQuestion approvedQuestion = createStudentQuestion(student, course)
        evaluateQuestion(true, false, approvedQuestion)
        studentQuestionRepository.save(approvedQuestion)

        and: 'a rejected question'
        StudentQuestion rejectedQuestion = createStudentQuestion(student, course)
        evaluateQuestion(false, true, rejectedQuestion)
        studentQuestionRepository.save(rejectedQuestion)


        when:
        List<StudentQuestionDTO> questions = studentCheckQuestionStatusService.getAllStudentQuestion(student.getId())

        then:
        questions.size() == 3;
        numberOfQuestionsWithStatus(questions, StudentQuestion.SubmittedStatus.APPROVED) == 1
        numberOfQuestionsWithStatus(questions, StudentQuestion.SubmittedStatus.REJECTED) == 1
        numberOfQuestionsWithStatus(questions, StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL) == 1

    }

    def numberOfQuestionsWithStatus(List<StudentQuestionDTO> questions, StudentQuestion.SubmittedStatus status) {
        def out = 0
        for(StudentQuestionDTO question : questions) {
            if(question.getSubmittedStatus() == status)
                out += 1
        }
        return out
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