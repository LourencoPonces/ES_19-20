package groovy.pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
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

class SignUpInTournamentTest extends Specification{
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOURNAMENT_TITLE = "tournament title"
    public static final String CREATOR_NAME = "user"
    public static final String CREATOR_USERNAME = "username"
    public static final String PARTICIPANT_NAME = "participant"
    public static final String PARTICIPANT_USERNAME = "participant username"

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
    def participant
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
        def creatorDto = new UserDto(creator)

        participant = new User(PARTICIPANT_NAME, PARTICIPANT_USERNAME, 1, User.Role.STUDENT)
        participant.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(participant)
        userRepository.save(participant)


        def topic = new Topic();
        topic.setName("TOPIC")
        topic.setCourse(course)
        topicRepository.save(topic)

        def topicDto = new TopicDto(topic)
        topicDtoList = new ArrayList<TopicDto>()
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


    def "sign-up in a tournament"() {
        given: "a tournament and a participant"
        tournamentService.createTournament(courseExecution.getId(), tournament)
        def participantDto = new UserDto(participant)

        when:
        tournamentService.signUpInTournament(tournament, participantDto)

        then:
        def tournamentCreated = tournamentRepository.findAll().get(0)
        def participants = tournamentCreated.getParticipants()
        participants.size() == 1
    }

    def "sign-up in a tournament although there aren't tournaments"() {
        given: "a participant"
        def participantDto = new UserDto(participant)

        when:
        tournamentService.signUpInTournament(tournament, participantDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_AVAILABLE
    }

    def "sign-up in a tournament with CREATED status"() {
        given: "a tournament with CREATED status and a participant"
        tournament.setStatus(Tournament.Status.CREATED)
        tournamentService.createTournament(courseExecution.getId(), tournament)
        def participantDto = new UserDto(participant)

        when:
        tournamentService.signUpInTournament(tournament, participantDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_AVAILABLE
    }

    def "sign-up in a tournament with RUNNING status"() {
        given: "a tournament with RUNNING status and a participant"
        tournament.setStatus(Tournament.Status.RUNNING)
        tournamentService.createTournament(courseExecution.getId(), tournament)
        def participantDto = new UserDto(participant)

        when:
        tournamentService.signUpInTournament(tournament, participantDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_AVAILABLE
    }

    def "sign-up in an tournament with FINISHED status"() {
        given: "a tournament with FINISHED status and a participant"
        tournament.setStatus(Tournament.Status.FINISHED)
        tournamentService.createTournament(courseExecution.getId(), tournament)
        def participantDto = new UserDto(participant)

        when:
        tournamentService.signUpInTournament(tournament, participantDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_AVAILABLE
    }

    def "sign-up in an tournament with CANCELLED status"() {
        given: "a tournament with CANCELED status and a participant"
        tournament.setStatus(Tournament.Status.CANCELLED)
        tournamentService.createTournament(courseExecution.getId(), tournament)
        def participantDto = new UserDto(participant)

        when:
        tournamentService.signUpInTournament(tournament, participantDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_AVAILABLE
    }

    def "sign-up in a tournament with a user that is already sign-up"(){
        given: "a tournament with a user already sign-up and a participant"
        tournamentService.createTournament(courseExecution.getId(), tournament)
        tournamentService.signUpInTournament(tournament, participantDto)
        def participantDto = new UserDto(participant)

        when:
        tournamentService.signUpInTournament(tournament, participantDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_ALREADY_SIGNUP_IN_TOURNAMENT
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}
