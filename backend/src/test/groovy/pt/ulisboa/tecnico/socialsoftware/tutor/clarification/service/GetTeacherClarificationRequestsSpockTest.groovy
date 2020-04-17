package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequestAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestAnswerRepository
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
    ClarificationRequestAnswerRepository clarificationRequestAnswerRepository

    Course course
    Question question

    CourseExecution courseExecution
    User teacher
    User student, student3
    Quiz quiz
    QuizAnswer quizAnswer, quizAnswer3
    QuizQuestion quizQuestion
    ClarificationRequest clarificationRequest, clarificationRequest3
    ClarificationRequestAnswer clarificationRequestAnswer

    CourseExecution courseExecution2
    User student2
    Quiz quiz2
    QuizQuestion quizQuestion2
    QuizAnswer quizAnswer2
    ClarificationRequest clarificationRequest2


    def setup() {
        course = createCourse("A course")
        question = createQuestion(1, course)

        courseExecution = createCourseExecution(course, "AC1", "1SEM")
        teacher = createUser(User.Role.TEACHER, 10, "t", courseExecution)
        quiz = createQuiz(10, courseExecution, Quiz.QuizType.GENERATED)
        quizQuestion = createQuizQuestion(quiz, question, 1)

        student = createUser(User.Role.STUDENT, 1, "s1", courseExecution)
        student3 = createUser(User.Role.STUDENT, 3, "s3", courseExecution)

        quizAnswer = createQuizAnswer(student, quiz)
        clarificationRequest = createClarificationRequest(1, student, question, "can we skip jmeter tests?")
        clarificationRequestAnswer = createClarificationRequestAnswer(teacher, clarificationRequest, "lol no")
        quizAnswer3 = createQuizAnswer(student3, quiz)
        clarificationRequest3 = createClarificationRequest(3, student3, question, "but why?")

        courseExecution2 = createCourseExecution(course, "AC1", "1SEM")
        quiz2 = createQuiz(20, courseExecution2, Quiz.QuizType.GENERATED)
        quizQuestion2 = createQuizQuestion(quiz2, question, 1)

        student2 = createUser(User.Role.STUDENT, 2, "s2", courseExecution2)
        quizAnswer2 = createQuizAnswer(student2, quiz2)
        clarificationRequest2 = createClarificationRequest(2, student2, question, "i really must skip jmeter tests")
    }

    private User createUser(User.Role role, int key, String username, CourseExecution courseExecution) {
        User user = new User();
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
        course.addQuestion(question)
        questionRepository.save(question)
        courseRepository.save(course)
        return question
    }

    private Course createCourse(String name) {
        def course = new Course()
        course.setName(name)
        courseRepository.save(course)
        return course
    }

    private Quiz createQuiz(int key, CourseExecution courseExecution, Quiz.QuizType type) {
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

    private ClarificationRequest createClarificationRequest(int key, User student, Question question, String content) {
        ClarificationRequestDto clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setContent(content)
        ClarificationRequest clarificationRequest = new ClarificationRequest(student, question, clarificationRequestDto)
        clarificationRequest.setKey(key)
        clarificationRequestRepository.save(clarificationRequest)
        return clarificationRequest
    }

    private ClarificationRequestAnswer createClarificationRequestAnswer(User teacher, ClarificationRequest clarificationRequest, String content) {
        ClarificationRequestAnswer clarificationRequestAnswer = new ClarificationRequestAnswer()
        clarificationRequestAnswer.setCreator(teacher)
        clarificationRequestAnswer.setRequest(clarificationRequest)
        clarificationRequestAnswer.setContent(content)
        clarificationRequest.setAnswer(clarificationRequestAnswer)

        clarificationRequestAnswerRepository.save(clarificationRequestAnswer)
        clarificationRequestRepository.save(clarificationRequest)
        return clarificationRequestAnswer
    }


    def "the teacher can see clarification requests submitted by students taking their classes"() {
        when:
        List<ClarificationRequestDto> requests = clarificationService.getTeacherClarificationRequests(teacher.getId())
        requests.sort(new Comparator<ClarificationRequestDto>() {
            @Override
            int compare(ClarificationRequestDto o1, ClarificationRequestDto o2) {
                int k1 = 0;
                int k2 = 0;
                if (o1 != null && o1.getKey() != null) k1 = o1.getKey();
                if (o2 != null && o2.getKey() != null) k2 = o2.getKey();
                return k1 - k2;
            }
        })

        then: "can only see requests from their students"
        requests.size() == 2
        requests[0].getCreationDateDate() == clarificationRequest.getCreationDate()
        requests[0].getContent() == clarificationRequest.getContent()
        requests[0].getOwner() == clarificationRequest.getOwner().getId()
        requests[0].getQuestionId() == clarificationRequest.getQuestion().getId()
        requests[0].getAnswer() != null
        requests[0].getAnswer().getCreationDateDate() == clarificationRequestAnswer.getCreationDate()
        requests[0].getAnswer().getCreatorId() == clarificationRequestAnswer.getCreator().getId()
        requests[0].getAnswer().getContent() == clarificationRequestAnswer.getContent()
        requests[1].getCreationDateDate() == clarificationRequest3.getCreationDate()
        requests[1].getContent() == clarificationRequest3.getContent()
        requests[1].getOwner() == clarificationRequest3.getOwner().getId()
        requests[1].getQuestionId() == clarificationRequest3.getQuestion().getId()
    }

    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        ClarificationService ClarificationService() {
            return new ClarificationService();
        }
    }
}
