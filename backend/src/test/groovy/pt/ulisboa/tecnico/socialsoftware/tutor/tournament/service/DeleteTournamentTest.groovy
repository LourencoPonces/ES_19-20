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
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DataJpaTest
class DeleteTournamentTest extends Specification {
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

    Tournament tournament
    TournamentDto tournamentDto
    User creator
    User participant
    Course course
    CourseExecution courseExecution
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

        participant = new User(PARTICIPANT_NAME, PARTICIPANT_USERNAME, 2, User.Role.STUDENT)
        participant.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(participant)
        userRepository.save(participant)

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
        topicDtoList = new ArrayList<TopicDto>()
        topicDtoList.add(topicDto)

        tournamentDto = new TournamentDto()
        tournamentDto.setTitle(TOURNAMENT_TITLE)
        tournamentDto.setKey(1)
        tournamentDto.setNumberOfQuestions(1)
        tournamentDto.setTopics(topicDtoList)

        def now = LocalDateTime.now()
        tournamentDto.setCreationDate(now.minusDays(2).format(formatter))
        tournamentDto.setAvailableDate(now.minusDays(1).format(formatter))
        tournamentDto.setRunningDate(now.plusDays(1).format(formatter))
        tournamentDto.setConclusionDate(now.plusDays(2).format(formatter))

        tournamentDto = tournamentService.createTournament(creator.getUsername(), courseExecution.getId(), tournamentDto);
        tournament = tournamentRepository.findById(tournamentDto.getId()).get()
    }

    def "Delete a tournament"() {
        when:
        tournamentService.deleteTournament(creator.getUsername(), tournament.getId());

        then:
        tournamentRepository.findById(tournament.getId()).isEmpty()
        creator.getCreatedTournaments().contains(tournament) == false
        participant.getParticipantTournaments().contains(tournament) == false
    }

    def "Delete non-existing tournament"() {
        given:
        int badId = 1000

        when:
        tournamentService.deleteTournament(creator.getUsername(), badId)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_FOUND
    }

    def "Delete tournament without having created it"() {
        when:
        tournamentService.deleteTournament(participant.getUsername(), tournament.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.MISSING_TOURNAMENT_OWNERSHIP
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
