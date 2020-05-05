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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.ImageRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_EVALUATE_PROMOTED_QUESTION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_JUSTIFICATION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_FOUND

@DataJpaTest
class TeacherPromotesStudentQuestionTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"

    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"

    public static final String USER_NAME = "ist199999"

    public static final Integer STUDENT_QUESTION_KEY = 1
    public static final Integer FAKE_STUDENT_QUESTION_ID = 2

    public static final String JUSTIFICATION = "very good question"

    public static final String QUESTION_TITLE = "Question title"
    public static final String NEW_QUESTION_TITLE = "New question title"

    public static final String OPTION_CONTENT = "Option content"
    public static final String QUESTION_CONTENT = "Question content"

    public static final String TOPIC_NAME = "topic name"
    public static final String NEW_TOPIC_NAME = "new topic name"
    public static final String NEW_OPTION_CONTENT = "new optionId content"
    public static final String NEW_QUESTION_CONTENT = 'new question content'
    public static final String URL = 'URL'


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

    @Autowired
    TopicRepository topicRepository

    @Autowired
    ImageRepository imageRepository

    @Autowired
    OptionRepository optionRepository

    def savedQuestionId
    def course
    def user
    def topic
    def studentQuestion
    def optionOK
    def optionKO

    def setup() {
        course = new Course()
        course.setName(COURSE_NAME)
        courseRepository.save(course)

        def courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        user = createTeacher(courseExecution)
        userRepository.save(user)


        createTopic()
        setUpStudentQuestion();
        savedQuestionId = studentQuestionRepository.findAll().get(0).getId()
    }

    def createTopic() {
        topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course)
        topicRepository.save(topic)
    }

    def setUpStudentQuestion() {
        studentQuestion =  new StudentQuestion();
        studentQuestion.addTopic(topic)
        studentQuestion.setKey(1);
        studentQuestion.setStudentQuestionKey(1);
        studentQuestion.setTitle(QUESTION_TITLE)
        studentQuestion.setContent(QUESTION_CONTENT)
        studentQuestion.setStatus(Question.Status.DISABLED)
        studentQuestion.setSubmittedStatus(StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL)
        studentQuestion.setNumberOfAnswers(2)
        studentQuestion.setNumberOfCorrect(1)
        studentQuestion.setCourse(course)
        studentQuestion.setUser(user)
        createOptions();
        createImage();
        course.addQuestion(studentQuestion)
        topic.addQuestion(studentQuestion)
        studentQuestionRepository.save(studentQuestion)
    }

    def createImage() {
        def image = new Image()
        image.setUrl(URL)
        image.setWidth(20)
        imageRepository.save(image)
        studentQuestion.setImage(image)
    }

    def createTeacher(CourseExecution courseExecution) {
        def user = new User()
        user.setKey(1)
        user.setUsername(USER_NAME)
        user.setRole(User.Role.TEACHER)
        user.getCourseExecutions().add(courseExecution)
        userRepository.save(user)
    }

    def createOptions() {
        optionOK = new Option()
        optionOK.setContent(OPTION_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setQuestion(studentQuestion)
        optionOK.setSequence(0)
        optionRepository.save(optionOK)
        optionKO = new Option()
        optionKO.setContent(OPTION_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setQuestion(studentQuestion)
        optionKO.setSequence(1)
        optionRepository.save(optionKO)
    }

    def createNewStudentQuestionDto() {
        def studentQuestionDto = new StudentQuestionDTO(studentQuestion)
        studentQuestionDto.setId(studentQuestion.getId())
        studentQuestionDto.setTitle(NEW_QUESTION_TITLE)
        studentQuestionDto.setContent(NEW_QUESTION_CONTENT)
        studentQuestionDto.setStatus(Question.Status.DISABLED.name())
        studentQuestionDto.setSubmittedStatus(StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL)
        studentQuestionDto.setNumberOfAnswers(4)
        studentQuestionDto.setNumberOfCorrect(2)
        studentQuestionDto.setUser(USER_NAME)
        return studentQuestionDto
    }

    def createNewTopic(Topic newTopic) {
        newTopic.setName(NEW_TOPIC_NAME)
        newTopic.setCourse(course)
        topicRepository.save(newTopic);
        def topicDto = new TopicDto(newTopic)
        def list = new ArrayList<TopicDto>()
        list.add(topicDto)
        return list
    }

    def createNewOption() {
        def optionDto = new OptionDto(optionKO)
        optionDto.setContent(NEW_OPTION_CONTENT)
        optionDto.setCorrect(false)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto(optionOK)
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        options.add(optionDto)
        return options
    }

    def "promote existing pending question with no justification"() {
        when:
        teacherEvaluatesStudentQuestionService.evaluateStudentQuestion(savedQuestionId, StudentQuestion.SubmittedStatus.PROMOTED, null)

        then:
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getSubmittedStatus() == StudentQuestion.SubmittedStatus.PROMOTED
        result.getJustification() == ""
    }

    def "promote existing pending question with valid justification"() {
        when:
        teacherEvaluatesStudentQuestionService.evaluateStudentQuestion(savedQuestionId, StudentQuestion.SubmittedStatus.PROMOTED, JUSTIFICATION)

        then:
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getSubmittedStatus() == StudentQuestion.SubmittedStatus.PROMOTED
        result.getJustification() == JUSTIFICATION
    }

    def "edit and promote existing student question"() {
        given: "a new student question"
        StudentQuestionDTO newStudentquestion = new StudentQuestionDTO(studentQuestionRepository.findAll().get(0))
        newStudentquestion.setTitle(NEW_QUESTION_TITLE)
        newStudentquestion.setJustification(JUSTIFICATION)

        when:
        teacherEvaluatesStudentQuestionService.updateAndPromoteStudentQuestion(courseRepository.findAll().get(0).getId(), savedQuestionId, newStudentquestion)

        then:
        def result = studentQuestionRepository.findAll().get(0)
        result.getTitle() == NEW_QUESTION_TITLE
        result.getSubmittedStatus() == StudentQuestion.SubmittedStatus.PROMOTED
        result.getJustification() == JUSTIFICATION

    }

    def "promote existing pending question with invalid justification"() {
        when:
        teacherEvaluatesStudentQuestionService.evaluateStudentQuestion(savedQuestionId, StudentQuestion.SubmittedStatus.PROMOTED, justification)

        then:
        def error = thrown(TutorException)
        error.errorMessage == result

        // invalid justifications:
        //   empty strings or null
        where:
        justification || result
        "   "         || INVALID_JUSTIFICATION
        "\n  \t"      || INVALID_JUSTIFICATION
    }

    def "promote already promoted student question"() {
        given: 'pending student question'
        studentQuestionRepository.count() == 1L
        def question = studentQuestionRepository.findAll().get(0)
        question.setSubmittedStatus(StudentQuestion.SubmittedStatus.PROMOTED);


        when:
        teacherEvaluatesStudentQuestionService.evaluateStudentQuestion(savedQuestionId, StudentQuestion.SubmittedStatus.PROMOTED, null)

        then:
        def error = thrown(TutorException)
        error.errorMessage == CANNOT_EVALUATE_PROMOTED_QUESTION

    }

    def "promote non existing student question"(){
        when:
        teacherEvaluatesStudentQuestionService.evaluateStudentQuestion(FAKE_STUDENT_QUESTION_ID, StudentQuestion.SubmittedStatus.PROMOTED, null)

        then:
        def error = thrown(TutorException)
        error.errorMessage == STUDENT_QUESTION_NOT_FOUND
    }


    @TestConfiguration
    static class TeacherEvaluatesImplTestContextConfiguration {

        @Bean
        TeacherEvaluatesStudentQuestionService teacherEvaluatesStudentQuestionService() {
            return  new TeacherEvaluatesStudentQuestionService();
        }
    }
}
