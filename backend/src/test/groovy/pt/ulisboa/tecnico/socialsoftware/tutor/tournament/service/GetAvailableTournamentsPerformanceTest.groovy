package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DataJpaTest
class GetAvailableTournamentsPerformanceTest extends Specification {
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOURNAMENT_TITLE = "tournament title"
    public static final Integer TOURNAMENT_KEY = 1
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

    TournamentDto tournamentDto
    User creator
    Course course
    CourseExecution courseExecution
    LocalDateTime creationDate
    LocalDateTime availableDate
    LocalDateTime runningDate
    LocalDateTime conclusionDate
    List<TopicDto> topicDtoList
    def formatter

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        (courseExecution, course) = setupCourse()

        setupCreator(courseExecution)

        topicDtoList = setupTopic(course)

        setupTournamentDto(formatter, topicDtoList)
    }

    private Tuple setupCourse() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)
        [courseExecution, course]
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

    private void setupTournamentDto(DateTimeFormatter formatter, ArrayList<TopicDto> topicDtoList) {
        tournamentDto = new TournamentDto()
        tournamentDto.setTitle(TOURNAMENT_TITLE)
        tournamentDto.setKey(TOURNAMENT_KEY)
        def now = LocalDateTime.now()
        creationDate = now.minusDays(2)
        availableDate = now.minusDays(1)
        runningDate = now.plusDays(1)
        conclusionDate = now.plusDays(2)
        tournamentDto.setNumberOfQuestions(1)
        tournamentDto.setCreationDate(creationDate.format(formatter))
        tournamentDto.setAvailableDate(availableDate.format(formatter))
        tournamentDto.setRunningDate(runningDate.format(formatter))
        tournamentDto.setConclusionDate(conclusionDate.format(formatter))
        tournamentDto.setTopics(topicDtoList)
    }

    def "performance testing to get available tournaments 1000 times"() {
        given:
        int numTournaments = 10
        1.upto(numTournaments, {
            tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournamentDto)
            tournamentDto.setKey(tournamentDto.getKey()+1)
        })

        int iterations = 1
        // iterations = 10000 // This is the desired value. It's commented out so that running every test
                             // doesn't take much time

        when:
        1.upto(iterations,{
            tournamentService.getAvailableTournaments(courseExecution.getId())
        })

        then:
        true
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}
