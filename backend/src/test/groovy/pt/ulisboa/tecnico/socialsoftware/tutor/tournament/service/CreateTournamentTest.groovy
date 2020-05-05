package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DataJpaTest
class CreateTournamentTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOURNAMENT_TITLE = "tournament title"
    public static final String CREATOR_NAME = "user"
    public static final String CREATOR_USERNAME = "username"

    @Autowired
    TournamentService tournamentService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    TournamentRepository tournamentRepository

    TournamentDto tournament
    User creator
    Course course
    CourseExecution courseExecution
    LocalDateTime creationDate
    LocalDateTime availableDate
    LocalDateTime runningDate
    LocalDateTime conclusionDate
    DateTimeFormatter formatter
    List<TopicDto> topicDtoList

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        (courseExecution, course) = setupCourse()

        UserDto creatorDto = setupCreator(courseExecution)

        topicDtoList = setupTopic(course)

        setupTournament(creatorDto, formatter, topicDtoList)
    }

    private Tuple setupCourse() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        new Tuple(courseExecution, course)
    }

    private UserDto setupCreator(CourseExecution courseExecution) {
        creator = new User(CREATOR_NAME, CREATOR_USERNAME, 1, User.Role.STUDENT)
        creator.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(creator)
        userRepository.save(creator)
        def creatorDto = new UserDto(creator);
        creatorDto
    }

    private ArrayList<TopicDto> setupTopic(Course course) {
        def topic = new Topic();
        topic.setName("TOPIC")
        topic.setCourse(course)
        topicRepository.save(topic)

        def topicDto = new TopicDto(topic)
        topicDtoList = new ArrayList<TopicDto>();
        topicDtoList.add(topicDto)
        topicDtoList
    }

    private void setupTournament(UserDto creatorDto, DateTimeFormatter formatter, ArrayList<TopicDto> topicDtoList) {
        tournament = new TournamentDto()
        tournament.setTitle(TOURNAMENT_TITLE)
        tournament.setKey(1)
        creationDate = LocalDateTime.now()
        availableDate = creationDate.plusDays(1)
        runningDate = creationDate.plusDays(2)
        conclusionDate = creationDate.plusDays(3)
        tournament.setNumberOfQuestions(1)
        tournament.setCreationDate(creationDate.format(formatter))
        tournament.setAvailableDate(availableDate.format(formatter))
        tournament.setRunningDate(runningDate.format(formatter))
        tournament.setConclusionDate(conclusionDate.format(formatter))
        tournament.setTopics(topicDtoList)
    }

    def "create a tournament"() {
        when:
        tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournament)

        then: "the correct tournament is inside the repository"
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getTitle() == TOURNAMENT_TITLE
        result.getCreationDate().format(formatter) == creationDate.format(formatter)
        result.getAvailableDate().format(formatter) == availableDate.format(formatter)
        result.getRunningDate().format(formatter) == runningDate.format(formatter)
        result.getConclusionDate().format(formatter) == conclusionDate.format(formatter)
        result.getStatus() == Tournament.Status.CREATED
        result.getCreator().getUsername() == CREATOR_USERNAME
        result.getTopics().size() == 1
        result.getParticipants().size() == 1
    }

    def "create a tournament with invalid number of questions"() {
        given: 'invalid number of questions'
        tournament.setNumberOfQuestions(0)

        when:
        tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournament)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        tournamentRepository.count() == 0L
    }

    def "create a tournament with a non-student"() {
        given: 'a teacher'
        creator.setRole(User.Role.TEACHER)

        when:
        tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournament)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_CREATED_BY_NON_STUDENT
        tournamentRepository.count() == 0L
    }

    def "create a tournament with a topic outside the course"() {
        given: 'a topic outside of the tournaments course'
        def outsideTopic = new Topic()
        outsideTopic.setName("OUTSIDE_TOPIC")
        topicRepository.save(outsideTopic)

        def topicDto = new TopicDto(outsideTopic)
        topicDtoList.add(topicDto)

        when:
        tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournament)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOPIC_NOT_FOUND
        tournamentRepository.count() == 0L
    }

    def "create a tournament with a student not enrolled in the course"() {
        given: 'A student not enrolled in course'

        def username = "UNREGISTERED_USERNAME"
        def unregisteredUser = new User("UNREGISTERED_NAME", username, 2, User.Role.STUDENT)
        userRepository.save(unregisteredUser)
        def unregisteredUserDto = new UserDto(unregisteredUser)

        tournament.setCreator(unregisteredUserDto)

        when:
        tournamentService.createTournament(username, courseExecution.getId(), tournament)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_ENROLLED_IN_COURSE_EXECUTION
        tournamentRepository.count() == 0L
    }

    def "create a tournament with a non-existing course"() {
        given: 'a bad courseId'
        def badCourseId = 2

        when:
        tournamentService.createTournament(CREATOR_USERNAME, badCourseId, tournament)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.COURSE_EXECUTION_NOT_FOUND
        tournamentRepository.count() == 0L
    }

    @Unroll("invalid dates: #availableDateDay | #runningDateDay | #conclusionDateDay || #errorMessage")
    def "invalid dates"() {
        given: "dates relative to creationDate"
        availableDate = creationDate.plusDays(availableDateDay)
        runningDate = creationDate.plusDays(runningDateDay)
        conclusionDate = creationDate.plusDays(conclusionDateDay)

        tournament.setAvailableDate(availableDate.format(formatter))
        tournament.setRunningDate(runningDate.format(formatter))
        tournament.setConclusionDate(conclusionDate.format(formatter))

        when:
        tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournament)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage
        tournamentRepository.count() == 0L

        where:
        availableDateDay | runningDateDay | conclusionDateDay || errorMessage
        1                | 3              | 2                 || ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        2                | 1              | 3                 || ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        2                | 3              | 1                 || ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        3                | 1              | 2                 || ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        3                | 2              | 1                 || ErrorMessage.TOURNAMENT_NOT_CONSISTENT
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

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

