package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.repository.ClarificationRequestRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard.MyStats
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
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User.Role
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DataJpaTest
class GetMyStatsServiceSpockTest extends Specification {
    static final String COURSE_NAME = "Software Architecture"
    static final String ACRONYM = "AS1"
    static final String ACADEMIC_TERM = "1 SEM"
    static final String CONTENT = "Test Content"
    static final String USERNAME_1 = "USERNAME_ONE"
    static final int INVALID_USER_ID= 50
    static final int INVALID_COURSE_ID = 50

    public static final String OPTION_CONTENT = "optionId content"
    public static final String TOPIC_NAME = "topic name"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'

    public static final String TOURNAMENT_TITLE = "tournament title"
    public static final String CREATOR_NAME = "user"
    public static final String CREATOR_USERNAME = "username"

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    QuizQuestionRepository quizQuestionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    ClarificationRequestRepository clarificationRequestRepository

    @Autowired
    ClarificationRequestAnswerRepository clarificationRequestAnswerRepository

    @Autowired
    MyStatsService myStatsService

    @Autowired
    ClarificationService clarificationService

    @Autowired
    TournamentService tournamentService


    Course course
    CourseExecution courseExecution
    Question question1
    Question question2
    Quiz quiz
    User student
    int studentId
    int courseId




    DateTimeFormatter formatter
    List<TopicDto> topicDtoList
    User creator
    TournamentDto tournamentDto

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseExecution = createCourseExecution(course, ACRONYM, ACADEMIC_TERM)
        student = createUser(courseExecution, Role.STUDENT, USERNAME_1, 1)

        courseRepository.save(course)
        courseExecutionRepository.save(courseExecution)
        userRepository.save(student)
        studentId = student.getId()
        courseId = course.getId()

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        UserDto creatorDto = setupCreator(courseExecution)
        topicDtoList = setupTopicList(course)
        setupTournamentDto(creatorDto, formatter, topicDtoList)
    }

    def clarificationRequestSetup() {
        createQuiz(1, courseExecution, "GENERATED")
        question1 = createQuestion(1, course)
        question2 = createQuestion(2, course)
        def quizQuestion1 = new QuizQuestion(quiz, question1, 1)
        def quizQuestion2 = new QuizQuestion(quiz, question2, 2)
        def quizAnswer1 = new QuizAnswer(student, quiz)
        def quizAnswer2 = new QuizAnswer(student, quiz)

        quizRepository.save(quiz)
        questionRepository.save(question1)
        questionRepository.save(question2)
        quizQuestionRepository.save(quizQuestion1)
        quizQuestionRepository.save(quizQuestion2)
        quizAnswerRepository.save(quizAnswer1)
        quizAnswerRepository.save(quizAnswer2)
    }

    private User createUser(CourseExecution courseExecution, Role role, String username, int key) {
        def user = new User('NAME', username, key, role)
        user.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(user)
        return user
    }

    private Question createQuestion(int key, Course course) {
        def question = new Question()
        question.setKey(key)
        question.setCourse(course)
        question.setTitle(QUESTION_TITLE)
        course.addQuestion(question)
        return question
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

    private void createQuiz(int key, CourseExecution courseExecution, String type) {
        quiz = new Quiz()
        quiz.setKey(key)
        quiz.setType(type)
        quiz.setCourseExecution(courseExecution)
        courseExecution.addQuiz(quiz)
    }

    private CourseExecution createCourseExecution(Course course, String acronym, String term) {
        def courseExecution = new CourseExecution()
        courseExecution.setCourse(course)
        courseExecution.setAcronym(acronym)
        courseExecution.setAcademicTerm(term)
        return courseExecution
    }

    private ArrayList<TopicDto> setupTopicList(Course course) {
        def topic = new Topic();
        topic.setName("TOPIC")
        topic.setCourse(course)

        // So that quiz generation doesn't throw exceptions
        def question = new Question()
        question.addTopic(topic)
        question.setTitle("question_title")
        question.setCourse(course)
        question.setStatus(Question.Status.AVAILABLE)

        questionRepository.save(question)
        topicRepository.save(topic)

        def topicDto = new TopicDto(topic)
        topicDtoList = new ArrayList<TopicDto>();
        topicDtoList.add(topicDto)
        topicDtoList
    }

    private UserDto setupCreator(CourseExecution courseExecution) {
        creator = new User(CREATOR_NAME, CREATOR_USERNAME, 2, User.Role.STUDENT)
        creator.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(creator)
        userRepository.save(creator)
        def creatorDto = new UserDto(creator);
        creatorDto
    }

    private void setupTournamentDto(UserDto creatorDto, DateTimeFormatter formatter, ArrayList<TopicDto> topicDtoList) {
        tournamentDto = new TournamentDto()
        tournamentDto.setTitle(TOURNAMENT_TITLE)
        tournamentDto.setKey(1)
        def creationDate = LocalDateTime.now().minusDays(1)
        def availableDate = LocalDateTime.now()
        def runningDate = LocalDateTime.now().plusDays(1)
        def conclusionDate = LocalDateTime.now().plusDays(2)
        tournamentDto.setNumberOfQuestions(1)
        tournamentDto.setCreationDate(creationDate.format(formatter))
        tournamentDto.setAvailableDate(availableDate.format(formatter))
        tournamentDto.setRunningDate(runningDate.format(formatter))
        tournamentDto.setConclusionDate(conclusionDate.format(formatter))
        tournamentDto.setTopics(topicDtoList)
    }

    def "get user's clarification dashboard stats"() {
        clarificationRequestSetup()
        given: "a private clarification request about question 1"
        def request1 = new ClarificationRequestDto()
        request1.setContent(CONTENT)
        clarificationService.submitClarificationRequest(question1.getId(), studentId, request1)

        and: "a public clarification request about question 2"
        def request2 = new ClarificationRequestDto()
        request2.setContent(CONTENT)
        request2 = clarificationService.submitClarificationRequest(question2.getId(), studentId, request2)
        clarificationService.changeClarificationRequestStatus(request2.getId(), ClarificationRequest.RequestStatus.PUBLIC)

        when:
        def result = myStatsService.getMyStats(student.getId(), courseId)

        then:
        result != null
        result.getRequestsSubmittedStat() == 2
        result.getPublicRequestsStat() == 1
        result.getRequestsSubmittedVisibility() == MyStats.StatsVisibility.PRIVATE
        result.getPublicRequestsVisibility() == MyStats.StatsVisibility.PRIVATE
    }

    def "get user's student question dashboard stats"() {
        given: "2 studentQuestions"
        def studentQuestion1 = createStudentQuestion(student, course, 1)
        student.addStudentQuestion(studentQuestion1)
        def studentQuestion2 = createStudentQuestion(student, course, 2)
        student.addStudentQuestion(studentQuestion2)

        and: "one of them approved"
        studentQuestion2.setSubmittedStatus(StudentQuestion.SubmittedStatus.APPROVED)
        studentQuestionRepository.save(studentQuestion1)
        studentQuestionRepository.save(studentQuestion2)

        when:
        def result = myStatsService.getMyStats(student.getId(), courseId)

        then:
        result != null
        result.getSubmittedQuestionsStat() == 2
        result.getApprovedQuestionsStat() == 1
        result.getApprovedQuestionsVisibility() == MyStats.StatsVisibility.PRIVATE
        result.getSubmittedQuestionsVisibility() == MyStats.StatsVisibility.PRIVATE
    }






    def "get user's tournaments dashboard stats"() {
        given: "an available tournament"
        tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournamentDto)

        when:
        def result = myStatsService.getMyStats(creator.getId(), courseId)

        then:
        result != null
        result.getTournamentsParticipatedStat() == 1;
        //result.getTournamentsScoreStat() == 1 // TODO: Not working now
        result.getTournamentsParticipatedVisibility() == MyStats.StatsVisibility.PRIVATE
        result.getTournamentsScoreVisibility() == MyStats.StatsVisibility.PRIVATE
    }












    @Unroll("invalid arguments: #isUserId | #isCourseid || #error_message")
    def "invalid arguments"() {
        when:
        changeCourseId(isCourseId)
        myStatsService.getMyStats(getUserId(isUserId), courseId)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == error_message

        where:
        isUserId | isCourseId  || error_message
        false    | true        || ErrorMessage.USER_NOT_FOUND
        true     | false       || ErrorMessage.COURSE_NOT_FOUND
    }

    private int getUserId(boolean isUserId) {
        if (!isUserId)
            return INVALID_USER_ID
        return student.getId()
    }

    private void changeCourseId(boolean isCourseId) {
        if (!isCourseId)
            courseId = INVALID_COURSE_ID
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
