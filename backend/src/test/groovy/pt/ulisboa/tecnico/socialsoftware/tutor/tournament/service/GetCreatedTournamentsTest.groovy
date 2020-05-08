package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
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
class GetCreatedTournamentsTest extends Specification {
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

    @Autowired
    QuestionRepository questionRepository

    TournamentDto tournamentDto
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

        topicDtoList = setupTopicList(course)

        setupTournamentDto(creatorDto, formatter, topicDtoList)
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
        creator = new User(CREATOR_NAME, CREATOR_USERNAME, 1, User.Role.STUDENT)
        creator.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(creator)
        userRepository.save(creator)
        def creatorDto = new UserDto(creator);
        creatorDto
    }

    private Tuple setupCourse() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        new Tuple(courseExecution, course)
    }

    private void setupTournamentDto(UserDto creatorDto, DateTimeFormatter formatter, ArrayList<TopicDto> topicDtoList) {
        tournamentDto = new TournamentDto()
        tournamentDto.setTitle(TOURNAMENT_TITLE)
        tournamentDto.setKey(1)
        creationDate = DateHandler.now().minusDays(1)
        availableDate = DateHandler.now()
        runningDate = DateHandler.now().plusDays(1)
        conclusionDate = DateHandler.now().plusDays(2)
        tournamentDto.setNumberOfQuestions(1)
        tournamentDto.setCreationDate(DateHandler.toISOString(creationDate))
        tournamentDto.setAvailableDate(DateHandler.toISOString(availableDate))
        tournamentDto.setRunningDate(DateHandler.toISOString(runningDate))
        tournamentDto.setConclusionDate(DateHandler.toISOString(conclusionDate))
        tournamentDto.setTopics(topicDtoList)
    }

    def "get the created tournaments"() {
        given: "an available tournament"
        tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournamentDto)

        when:
        def tournamentsList = tournamentService.getCreatedTournaments(creator.getId(), courseExecution.getId())

        then: "there are only one tournament in the list"
        tournamentsList.size() == 1
        and: "the tournament in the list have the correct data"
        def tournamentElement = tournamentsList.get(0)
        tournamentElement.getId() != null
        tournamentElement.getKey() != null
        tournamentElement.getTitle() == TOURNAMENT_TITLE
        tournamentElement.getCreationDate() != null
        tournamentElement.getAvailableDate() == DateHandler.toISOString(availableDate)
        tournamentElement.getRunningDate() == DateHandler.toISOString(runningDate)
        tournamentElement.getConclusionDate() == DateHandler.toISOString(conclusionDate)
        tournamentElement.getStatus() == Tournament.Status.AVAILABLE
        tournamentElement.getCreator().getUsername() == CREATOR_USERNAME
        tournamentElement.getTopics().size() == 1
        tournamentElement.getParticipants().size() == 1
    }

    def "get the created tournaments, although there are not any"() {
        when:
        def tournamentsList = tournamentService.getCreatedTournaments(creator.getId(), courseExecution.getId())

        then: "There are no tournaments"
        tournamentsList.size() == 0
    }


    def "get the created tournaments with a non-existing id"() {
        given: 'a bad userId'
        def badUserId = 12345678

        when:
        tournamentService.getCreatedTournaments(badUserId, courseExecution.getId())

        then: ""
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_FOUND
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
