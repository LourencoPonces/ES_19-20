package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationMessageRepository
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

@DataJpaTest
class GetTeacherClarificationRequestsSpockTest extends Specification {
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
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    ClarificationRequestRepository clarificationRequestRepository

    @Autowired
    ClarificationService clarificationService

    @Autowired
    ClarificationMessageRepository clarificationMessageRepository

    Course course
    Question question

    CourseExecution courseExecution
    User teacher
    User student, student3
    Quiz quiz
    QuizAnswer quizAnswer, quizAnswer3
    QuizQuestion quizQuestion
    ClarificationRequest clarificationRequest, clarificationRequest3
    ClarificationMessage clarificationReply

    CourseExecution courseExecution2
    User student2
    Quiz quiz2
    QuizQuestion quizQuestion2
    QuizAnswer quizAnswer2
    ClarificationRequest clarificationRequest2


    def setup() {
        course = new Course("A course", Course.Type.TECNICO)
        question = createQuestion(1, course)

        courseExecution = createCourseExecution(course, "AC1", "1SEM")
        teacher = createUser(User.Role.TEACHER, 10, "t", courseExecution)
        quiz = createQuiz(10, courseExecution, "GENERATED")
        quizQuestion = createQuizQuestion(quiz, question, 1)

        student = createUser(User.Role.STUDENT, 1, "s1", courseExecution)
        student3 = createUser(User.Role.STUDENT, 3, "s3", courseExecution)

        quizAnswer = createQuizAnswer(student, quiz)
        clarificationRequest = createClarificationRequest(1, student, question, "can we skip jmeter tests?", null)
        clarificationReply = createClarificationMessage(teacher, clarificationRequest, "lol no")
        quizAnswer3 = createQuizAnswer(student3, quiz)
        clarificationRequest3 = createClarificationRequest(3, student3, question, "but why?", null)

        courseExecution2 = createCourseExecution(course, "AC1", "1SEM")
        quiz2 = createQuiz(20, courseExecution2, "GENERATED")
        quizQuestion2 = createQuizQuestion(quiz2, question, 1)

        student2 = createUser(User.Role.STUDENT, 2, "s2", courseExecution2)
        quizAnswer2 = createQuizAnswer(student2, quiz2)
        clarificationRequest2 = createClarificationRequest(2, student2, question, "i really must skip jmeter tests", ClarificationRequest.RequestType.TYPO)
    }

    private User createUser(User.Role role, int key, String username, CourseExecution courseExecution) {
        User user = new User()
        user.setRole(role)
        user.setKey(key)
        user.setName(username)
        user.setUsername(username)
        user.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(user)
        userRepository.save(user)
        courseExecutionRepository.save(courseExecution)
        return user
    }

    private Question createQuestion(int key, Course course) {
        def question = new Question()
        question.setKey(key)
        question.setCourse(course)
        question.setTitle("TITLE")
        course.addQuestion(question)
        questionRepository.save(question)
        courseRepository.save(course)
        return question
    }

    private Quiz createQuiz(int key, CourseExecution courseExecution, String type) {
        Quiz quiz = new Quiz()
        quiz.setKey(key)
        quiz.setType(type)
        quiz.setCourseExecution(courseExecution)
        courseExecution.addQuiz(quiz)
        quizRepository.save(quiz)
        courseExecutionRepository.save(courseExecution)
        return quiz
    }

    private QuizQuestion createQuizQuestion(Quiz quiz, Question question, int sequence) {
        QuizQuestion quizQuestion = new QuizQuestion(quiz, question, sequence)
        quizQuestionRepository.save(quizQuestion)
        return quizQuestion
    }

    private CourseExecution createCourseExecution(Course course, String acronym, String term) {
        CourseExecution courseExecution = new CourseExecution()
        courseExecution.setCourse(course)
        courseExecution.setAcronym(acronym)
        courseExecution.setAcademicTerm(term)
        courseExecutionRepository.save(courseExecution)
        return courseExecution
    }

    private QuizAnswer createQuizAnswer(User student, Quiz quiz) {
        QuizAnswer quizAnswer = new QuizAnswer(student, quiz)
        quizAnswerRepository.save(quizAnswer)
        return quizAnswer
    }

    private ClarificationRequest createClarificationRequest(int key, User student, Question question, String content, ClarificationRequest.RequestType type) {
        ClarificationRequestDto clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setContent(content)
        clarificationRequestDto.setType(type)
        ClarificationRequest clarificationRequest = new ClarificationRequest(question, student, clarificationRequestDto)
        clarificationRequest.setKey(key)
        clarificationRequestRepository.save(clarificationRequest)
        return clarificationRequest
    }

    private ClarificationMessage createClarificationMessage(User teacher, ClarificationRequest clarificationRequest, String content) {
        ClarificationMessage message = new ClarificationMessage()
        message.setCreator(teacher)
        message.setRequest(clarificationRequest)
        message.setContent(content)

        clarificationRequest.getMessages().add(message)

        clarificationMessageRepository.save(message)
        clarificationRequestRepository.save(clarificationRequest)
        return message
    }


    def "the teacher can see clarification requests submitted by students taking their classes"() {
        when:
        def result = clarificationService.getTeacherClarificationRequests(teacher.id)
        List<ClarificationRequestDto> requests = result.requests
        requests.sort(Comparator.comparing { r -> ((ClarificationRequestDto) r).id })

        then: "can only see requests from their students"
        requests.size() == 2
        requests[0].getCreationDateDate() == clarificationRequest.creationDate
        requests[0].content == clarificationRequest.content
        requests[0].getCreatorUsername() == clarificationRequest.creator.username
        requests[0].getQuestionId() == clarificationRequest.question.id
        requests[0].getMessages() != null
        requests[0].getMessages().size() == 1
        def reply0 = requests[0].getMessages().get(0)
        reply0.getCreationDateDate() == clarificationReply.creationDate
        reply0.getCreatorUsername() == clarificationReply.creator.username
        reply0.content == clarificationReply.content
        requests[1].getCreationDateDate() == clarificationRequest3.creationDate
        requests[1].content == clarificationRequest3.content
        requests[1].getCreatorUsername() == clarificationRequest3.creator.username
        requests[1].getQuestionId() == clarificationRequest3.question.id

        and: "user information is returned"
        result.names.get(clarificationRequest.creator.username) == clarificationRequest.creator.name
        result.names.get(clarificationReply.creator.username) == clarificationReply.creator.name
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService()
        }
    }
}
