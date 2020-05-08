package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard.MyStats
import pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard.MyStatsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard.MyStatsService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class UpdateMyStatsVisibilitySpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"
    static final String USERNAME_1 = "USERNAME_ONE"

    public static final String OPTION_CONTENT = "optionId content"
    public static final String TOPIC_NAME = "topic name"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"

    static final String REQUEST_CONTENT = "request Content"


    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    QuizRepository quizRepository


    @Autowired
    QuizQuestionRepository quizQuestionRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    ClarificationRequestRepository clarificationRequestRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    ClarificationService clarificationService

    @Autowired
    MyStatsService myStatsService

    Course course
    CourseExecution courseExecution
    Quiz quiz
    User student
    Question question1
    Question question2
    int studentId
    int courseId

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        quiz = createQuiz(1, courseExecution, "GENERATED")
        question1 = createQuestion(1, course)
        question2 = createQuestion(2, course)
        QuizQuestion quizQuestion1 = new QuizQuestion(quiz, question1, 1)
        QuizQuestion quizQuestion2 = new QuizQuestion(quiz, question2, 2)
        student = createUser(courseExecution, User.Role.STUDENT, USERNAME_1, 1)
        QuizAnswer quizAnswer = new QuizAnswer(student, quiz)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        quizRepository.save(quiz)
        questionRepository.save(question1)
        questionRepository.save(question2)
        quizQuestionRepository.save(quizQuestion1)
        quizQuestionRepository.save(quizQuestion2)
        userRepository.save(student)
        quizAnswerRepository.save(quizAnswer)
        studentId = student.getId()
        courseId = course.getId()
    }

    private CourseExecution createCourseExecution(Course course, String acronym, String term) {
        def courseExecution = new CourseExecution()
        courseExecution.setCourse(course)
        courseExecution.setAcronym(acronym)
        courseExecution.setAcademicTerm(term)
        return courseExecution
    }

    private Question createQuestion(int key, Course course) {
        def question = new Question()
        question.setKey(key)
        question.setCourse(course)
        question.setTitle("TITLE")
        course.addQuestion(question)
        return question
    }

    private Quiz createQuiz(int key, CourseExecution courseExecution, String type) {
        def quiz = new Quiz()
        quiz.setKey(key)
        quiz.setType(type)
        quiz.setCourseExecution(courseExecution)
        courseExecution.addQuiz(quiz)
        return quiz
    }

    private User createUser(CourseExecution courseExecution, User.Role role, String username, int key) {
        def user = new User('NAME', username, key, role)
        user.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(user)
        return user
    }

    private StudentQuestion createStudentQuestion(User user, Course course, int key) {
        def studentQuestion = new StudentQuestionDTO()
        studentQuestion.setTitle(QUESTION_TITLE)
        studentQuestion.setContent(QUESTION_CONTENT)
        studentQuestion.setStatus(Question.Status.DISABLED.name())
        studentQuestion.setKey(key)
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
        def topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course)
        topicRepository.save(topic)
        def topicDto = new TopicDto(topic)
        def topicList = new ArrayList<TopicDto>()
        topicList.add(topicDto)
        studentQuestion.setTopics(topicList)
    }

    private ClarificationRequestDto createClarificationRequestDto(String content) {
        def clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setContent(content)
        return clarificationRequestDto
    }

    def "change submitted and public requests' visibility to public"() {
        given: "1 private clarification request"
        def req1 = createClarificationRequestDto(REQUEST_CONTENT)
        clarificationService.submitClarificationRequest(question1.getId(), studentId, req1)

        and: "1 public clarification request"
        def req2 = createClarificationRequestDto(REQUEST_CONTENT)
        req2 = clarificationService.submitClarificationRequest(question2.getId(), studentId, req2)
        clarificationService.changeClarificationRequestStatus(req2.getId(), ClarificationRequest.RequestStatus.PUBLIC)

        and: "the stats changed to public"
        MyStatsDto myStatsDto = myStatsService.getMyStats(student.getId(), courseId)
        myStatsDto.setRequestsSubmittedVisibility(MyStats.StatsVisibility.PUBLIC)
        myStatsDto.setPublicRequestsVisibility(MyStats.StatsVisibility.PUBLIC)

        when:
        def result = myStatsService.updateVisibility(myStatsDto.getId(), myStatsDto)

        then:
        result != null
        result.getRequestsSubmittedStat() == 2
        result.getPublicRequestsStat() == 1
        result.getRequestsSubmittedVisibility() == MyStats.StatsVisibility.PUBLIC
        result.getPublicRequestsVisibility() == MyStats.StatsVisibility.PUBLIC
    }

    def "change submitted and public requests' visibility to private"() {
        given: "1 private clarification request"
        def req1 = createClarificationRequestDto(REQUEST_CONTENT)
        clarificationService.submitClarificationRequest(question1.getId(), studentId, req1)

        and: "1 public clarification request"
        def req2 = createClarificationRequestDto(REQUEST_CONTENT)
        req2 = clarificationService.submitClarificationRequest(question2.getId(), studentId, req2)
        clarificationService.changeClarificationRequestStatus(req2.getId(), ClarificationRequest.RequestStatus.PUBLIC)

        and: "the stats changed to private"
        MyStatsDto myStatsDto = myStatsService.getMyStats(student.getId(), courseId)
        myStatsDto.setRequestsSubmittedVisibility(MyStats.StatsVisibility.PRIVATE)
        myStatsDto.setPublicRequestsVisibility(MyStats.StatsVisibility.PRIVATE)

        when:
        def result = myStatsService.updateVisibility(myStatsDto.getId(), myStatsDto)

        then:
        result != null
        result.getRequestsSubmittedStat() == 2
        result.getPublicRequestsStat() == 1
        result.getRequestsSubmittedVisibility() == MyStats.StatsVisibility.PRIVATE
        result.getPublicRequestsVisibility() == MyStats.StatsVisibility.PRIVATE
    }

    def "change submitted and approved questions' visibility to public"() {
        given: "2 studentQuestions"
        def studentQuestion1 = createStudentQuestion(student, course, 1)
        student.addStudentQuestion(studentQuestion1)
        def studentQuestion2 = createStudentQuestion(student, course, 2)
        student.addStudentQuestion(studentQuestion2)

        and: "one of them approved"
        studentQuestion2.setSubmittedStatus(StudentQuestion.SubmittedStatus.APPROVED)
        studentQuestionRepository.save(studentQuestion1)
        studentQuestionRepository.save(studentQuestion2)

        and: "the stats changed to public"
        MyStatsDto myStatsDto = myStatsService.getMyStats(student.getId(), courseId)
        myStatsDto.setApprovedQuestionsVisibility(MyStats.StatsVisibility.PUBLIC)
        myStatsDto.setSubmittedQuestionsVisibility(MyStats.StatsVisibility.PUBLIC)

        when:
        def result = myStatsService.updateVisibility(myStatsDto.getId(), myStatsDto)

        then:
        result != null
        result.getSubmittedQuestionsStat() == 2
        result.getApprovedQuestionsStat() == 1
        result.getApprovedQuestionsVisibility() == MyStats.StatsVisibility.PUBLIC
        result.getSubmittedQuestionsVisibility() == MyStats.StatsVisibility.PUBLIC
    }

    def "change submitted and approved questions visibility to private"() {
        given: "2 studentQuestions"
        def studentQuestion1 = createStudentQuestion(student, course, 1)
        student.addStudentQuestion(studentQuestion1)
        def studentQuestion2 = createStudentQuestion(student, course, 2)
        student.addStudentQuestion(studentQuestion2)

        and: "one of them approved"
        studentQuestion2.setSubmittedStatus(StudentQuestion.SubmittedStatus.APPROVED)
        studentQuestionRepository.save(studentQuestion1)
        studentQuestionRepository.save(studentQuestion2)

        and: "the stats changed to private"
        MyStatsDto myStatsDto = myStatsService.getMyStats(student.getId(), courseId)
        myStatsDto.setApprovedQuestionsVisibility(MyStats.StatsVisibility.PRIVATE)
        myStatsDto.setSubmittedQuestionsVisibility(MyStats.StatsVisibility.PRIVATE)

        when:
        def result = myStatsService.updateVisibility(myStatsDto.getId(), myStatsDto)


        then:
        result != null
        result.getSubmittedQuestionsStat() == 2
        result.getApprovedQuestionsStat() == 1
        result.getApprovedQuestionsVisibility() == MyStats.StatsVisibility.PRIVATE
        result.getSubmittedQuestionsVisibility() == MyStats.StatsVisibility.PRIVATE

    }

    def "try to get stats that don't exist"() {
        def nonExistingMyStatsId = 100000
        def emptyDto = new MyStatsDto()

        when:
        myStatsService.updateVisibility(nonExistingMyStatsId, emptyDto)

        then: "Error is thrown"
        def error = thrown(TutorException)
        error.errorMessage == ErrorMessage.NO_MY_STATS_FOUND
    }


    @TestConfiguration
    static class ClarificationServiceImplTestContextConfiguration {

        @Bean
        MyStatsService myStatsService() {
            return new MyStatsService();
        }

        @Bean
        ClarificationService clarificationService() {
            return new ClarificationService();
        }

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }

        @Bean
        QuizService quizService() {
            return new QuizService()
        }

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }

        @Bean
        AnswerService answerService() {
            return new AnswerService()
        }

        @Bean
        AnswersXmlImport answersXmlImport() {
            return new AnswersXmlImport()
        }
    }
}
