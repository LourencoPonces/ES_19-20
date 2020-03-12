package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DataJpaTest
class GetAvailableTournamentsTest extends Specification {
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

    def tournament
    def creator
    def course
    def courseExecution
    def creationDate
    def availableDate
    def runningDate
    def conclusionDate
    def formatter
    def topicDtoList

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        creator = new User(CREATOR_NAME, CREATOR_USERNAME, 1, User.Role.STUDENT)
        creator.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(creator)
        userRepository.save(creator)
        def creatorDto = new UserDto(creator);

        def topic = new Topic();
        topic.setName("TOPIC")
        topic.setCourse(course)
        topicRepository.save(topic)

        def topicDto = new TopicDto(topic)
        topicDtoList = new ArrayList<TopicDto>();
        topicDtoList.add(topicDto)

        tournament = new TournamentDto()
        tournament.setTitle(TOURNAMENT_TITLE)
        tournament.setKey(1)
        creationDate = LocalDateTime.now()
        availableDate = LocalDateTime.now().plusDays(1)
        runningDate = LocalDateTime.now().plusDays(2)
        conclusionDate = LocalDateTime.now().plusDays(3)
        tournament.setNumberOfQuestions(1)
        tournament.setCreator(creatorDto)
        tournament.setCreationDate(creationDate.format(formatter))
        tournament.setAvailableDate(availableDate.format(formatter))
        tournament.setRunningDate(runningDate.format(formatter))
        tournament.setConclusionDate(conclusionDate.format(formatter))
        tournament.setTopics(topicDtoList)
    }

    def "get the available tournaments"() {
        given:
        tournamentService.createTournament(courseExecution.getId(), tournament)

        when:
        def tournamentsList = tournamentService.getAvailableTournaments(courseExecution.getId(), tournament)


        then: "there are only one tournament in the list"
        tournamentsList.size() == 1
        and: "the tournament in the list have the correct data"
        def tournamentElement = tournamentsList.get(0)
        tournamentElement.getId() != null
        tournamentElement.getKey() != null
        tournamentElement.getTitle() == TOURNAMENT_TITLE
        tournamentElement.getCreationDate() != null
        tournamentElement.getAvailableDate().format(formatter) == availableDate.format(formatter)
        tournamentElement.getRunningDate().format(formatter) == runningDate.format(formatter)
        tournamentElement.getConclusionDate().format(formatter) == conclusionDate.format(formatter)
        tournamentElement.getStatus() == Tournament.Status.CREATED
        tournamentElement.getCreator().getUsername() == CREATOR_USERNAME
        tournamentElement.getTopics().size() == 1
        tournamentElement.getParticipants().size() == 1
    }

    def "get the available tournaments, although there are not any"() {
        when:
        def tournamentsList = tournamentService.getAvailableTournaments(courseExecution.getId())

        then:
        tournamentsList.size() == 0
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_AVAILABLE
    }

    def "get the available tournaments, although there are only tournaments in CREATED status"(){
        given: "a tournament with CREATED status"
        tournament.setStatus(Tournament.Status.CREATED)

        when:
        def tournamentsList = tournamentService.getAvailableTournaments(courseExecution.getId())

        then:
        tournamentsList.size() == 0
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_AVAILABLE
    }

    def "get the available tournaments, although there are only tournaments in RUNNING status"(){
        given: "a tournament with RUNNING status"
        tournament.setStatus(Tournament.Status.RUNNING)

        when:
        def tournamentsList = tournamentService.getAvailableTournaments(courseExecution.getId())

        then:
        tournamentsList.size() == 0
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_AVAILABLE
    }

    def "get the available tournaments, although there are only tournaments in FINISHED status"(){
        given: "a tournament with FINISHED status"
        tournament.setStatus(Tournament.Status.FINISHED)

        when:
        def tournamentsList = tournamentService.getAvailableTournaments(courseExecution.getId())

        then:
        tournamentsList.size() == 0
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_AVAILABLE
    }

    //TODO: Is this repetition of the code above?
    /*
    def "get the available tournaments, although there are only tournaments with available date after the current date"(){
        // exception is thrown
        expect: false
    }

    def "get the available tournaments, although there are only tournaments with conclusion date before the current date"(){
        // exception is thrown
        expect: false
    }*/

    def "get the available tournaments with a non-existing course"(){
        //thrown exception
        expect: false


        given: 'a bad courseId'
        def badCourseId = 2

        when:
        tournamentService.getAvailableTournaments(badCourseId)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.COURSE_EXECUTION_NOT_FOUND
    }

    //TODO: Test Configuration
    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}
