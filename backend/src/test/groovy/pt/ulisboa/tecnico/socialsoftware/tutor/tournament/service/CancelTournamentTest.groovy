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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DataJpaTest
class CancelTournamentTest extends Specification {
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

    @Autowired
    QuestionRepository questionRepository

    def tournamentDto
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
    def question

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

        participant = new User(PARTICIPANT_NAME, PARTICIPANT_USERNAME, 2, User.Role.STUDENT)
        participant.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(participant)
        userRepository.save(participant)

        def topic = new Topic();
        topic.setName("TOPIC")
        topic.setCourse(course)

        // So that quiz generation doesn't throw exceptions
        question = new Question()
        question.addTopic(topic)
        question.setTitle("question_title")
        question.setCourse(course)
        question.setStatus(Question.Status.AVAILABLE)

        questionRepository.save(question)
        topicRepository.save(topic)

        def topicDto = new TopicDto(topic)
        topicDtoList = new ArrayList<TopicDto>()
        topicDtoList.add(topicDto)

        tournamentDto = new TournamentDto()
        tournamentDto.setTitle(TOURNAMENT_TITLE)
        tournamentDto.setKey(1)
        tournamentDto.setNumberOfQuestions(1)
        tournamentDto.setTopics(topicDtoList)
    }

    @Unroll("allowed status: #status || #newStatus")
    def "cancel a tournament with an allowed status"() {
        given: "a tournament with a not allowed status"
        prepareStatus(tournamentDto, status)
        tournamentDto = tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournamentDto)

        when:
        tournamentService.cancelTournament(creator.getUsername(), tournamentDto.getId());

        then:
        def tournamentCanceled = tournamentRepository.findAll().get(0)
        tournamentCanceled.getStatus() == newStatus
        tournamentCanceled.isCancelled() == isCancelled

        where:
        status                      || newStatus                   || isCancelled
        Tournament.Status.CREATED   || Tournament.Status.CANCELLED || true
        Tournament.Status.AVAILABLE || Tournament.Status.CANCELLED || true
    }

    @Unroll("not allowed status: #status || #errorMessage")
    def "cancel a tournament with a not allowed status"() {
        given: "a tournament with a not allowed status"
        prepareStatus(tournamentDto, status)
        tournamentDto = tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournamentDto)

        when:
        tournamentService.cancelTournament(creator.getUsername(), tournamentDto.getId());

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage
        def tournamentNotCanceled = tournamentRepository.findAll().get(0)
        tournamentNotCanceled.getStatus() == newStatus
        tournamentNotCanceled.isCancelled() == isCancelled

        where:
        status                      || errorMessage                               || newStatus                   || isCancelled
        Tournament.Status.RUNNING   || ErrorMessage.TOURNAMENT_CANNOT_BE_CANCELED || Tournament.Status.RUNNING   || false
        Tournament.Status.FINISHED  || ErrorMessage.TOURNAMENT_CANNOT_BE_CANCELED || Tournament.Status.FINISHED  || false
        Tournament.Status.CANCELLED || ErrorMessage.TOURNAMENT_CANNOT_BE_CANCELED || Tournament.Status.CANCELLED || true
    }

    def "Cancel non-existing tournament"() {
        given:
        int badId = 1000

        when:
        tournamentService.cancelTournament(creator.getUsername(), badId)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_FOUND
    }

    def prepareStatus(TournamentDto tournamentDto, Tournament.Status status) {
        def now = LocalDateTime.now()

        switch(status) {
            case Tournament.Status.CREATED:
                creationDate = now.minusDays(1)
                availableDate = now.plusDays(1)
                runningDate = now.plusDays(2)
                conclusionDate = now.plusDays(3)
                break;
            case Tournament.Status.AVAILABLE:
                creationDate = now.minusDays(2)
                availableDate = now.minusDays(1)
                runningDate = now.plusDays(1)
                conclusionDate = now.plusDays(2)
                break;
            case Tournament.Status.RUNNING:
                creationDate = now.minusDays(3)
                availableDate = now.minusDays(2)
                runningDate = now.minusDays(1)
                conclusionDate = now.plusDays(1)
                break;
            case Tournament.Status.FINISHED:
                creationDate = now.minusDays(4)
                availableDate = now.minusDays(3)
                runningDate = now.minusDays(2)
                conclusionDate = now.minusDays(1)
                break;
            case Tournament.Status.CANCELLED:
                creationDate = now.minusDays(4)
                availableDate = now.minusDays(3)
                runningDate = now.minusDays(2)
                conclusionDate = now.minusDays(1)
                tournamentDto.setIsCancelled();
        }

        tournamentDto.setCreationDate(creationDate.format(formatter))
        tournamentDto.setAvailableDate(availableDate.format(formatter))
        tournamentDto.setRunningDate(runningDate.format(formatter))
        tournamentDto.setConclusionDate(conclusionDate.format(formatter))
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
