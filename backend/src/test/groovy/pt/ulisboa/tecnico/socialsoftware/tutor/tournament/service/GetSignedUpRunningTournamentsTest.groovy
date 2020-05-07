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
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@DataJpaTest
class GetSignedUpRunningTournamentsTest extends Specification {
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

    def tournamentDto
    def creator
    def course
    def courseExecution
    def now
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

        def topic = new Topic();
        topic.setName("TOPIC")
        topic.setCourse(course)

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

        now = LocalDateTime.now()
        creationDate = now.minusDays(3)
        availableDate = now.minusDays(2)
        runningDate = now.minusDays(1)
        conclusionDate = now.plusDays(2)
        tournamentDto.setCreationDate(creationDate.format(formatter))
        tournamentDto.setAvailableDate(availableDate.format(formatter))
        tournamentDto.setRunningDate(runningDate.format(formatter))
        tournamentDto.setConclusionDate(conclusionDate.format(formatter))
    }


    def "Get signed-up running tournaments"() {
        given: "A running tournament"
        tournamentDto = tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournamentDto)

        when:
        def tournamentList = tournamentService.getSignedUpRunningTournaments(creator.getId())

        then:
        tournamentList.get(0).getId() == tournamentDto.getId()
    }

    def "Signed-up non-running tournament"() {
        given: "An available tournament"
        runningDate = now.plusDays(1)
        tournamentDto.setRunningDate(runningDate.format(formatter))
        tournamentDto = tournamentService.createTournament(CREATOR_USERNAME, courseExecution.getId(), tournamentDto)

        when:
        def tournamentList = tournamentService.getSignedUpRunningTournaments(creator.getId())

        then:
        tournamentList.isEmpty()
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
