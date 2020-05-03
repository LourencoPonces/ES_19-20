package pt.ulisboa.tecnico.socialsoftware.tutor.statement.service

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
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.*
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll
import java.time.LocalDateTime

@DataJpaTest
class GetSolvedQuizzesTest extends Specification {
    static final USERNAME = 'username'
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUIZ_TITLE = "Quiz title"
    public static final LocalDateTime BEFORE = DateHandler.now().minusDays(2)
    public static final LocalDateTime YESTERDAY = DateHandler.now().minusDays(1)
    public static final LocalDateTime TODAY = DateHandler.now()
    public static final LocalDateTime TOMORROW = DateHandler.now().plusDays(1)
    public static final LocalDateTime LATER = DateHandler.now().plusDays(2)

    @Autowired
    QuizService quizService

    @Autowired
    AnswerService answerService

    @Autowired
    StatementService statementService

    @Autowired
    ClarificationService clarificationService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    QuestionAnswerRepository questionAnswerRepository

    def user
    def courseDto
    def question
    def option
    def quiz
    def quizQuestion
    def quizAnswer
    def questionAnswer
    def course
    def courseExecution

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        courseDto = new CourseDto(courseExecution)

        user = new User('name', USERNAME, 1, User.Role.STUDENT)
        user.getCourseExecutions().add(courseExecution)
        courseExecution.getUsers().add(user)

        createQuestion()
        createOption()

        userRepository.save(user)
        questionRepository.save(question)
    }

    private void createOption() {
        option = new Option()
        option.setContent("Option Content")
        option.setCorrect(true)
        option.setSequence(0)
        option.setQuestion(question)
    }

    private void createQuestion() {
        question = new Question()
        question.setKey(1)
        question.setCourse(course)
        question.setContent("Question Content")
        question.setTitle("Question Title")
    }

    private void createQuestionAnswer() {
        questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswer.setOption(option)
    }

    private void createQuizAnswer() {
        quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)
    }

    private void createQuizQuestion() {
        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
    }

    private void createQuiz(Quiz.QuizType quizType, LocalDateTime conclusionDate, LocalDateTime resultsDate) {
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(quizType.toString())
        quiz.setAvailableDate(BEFORE)
        quiz.setConclusionDate(conclusionDate)
        quiz.setResultsDate(resultsDate)
        quiz.setCourseExecution(courseExecution)
    }

    @Unroll
    def "returns solved quiz with: quizType=#quizType | conclusionDate=#conclusionDate | resultsDate=#resultsDate"() {
        given: "a quiz answered by the user"
        createQuiz(Quiz.QuizType.PROPOSED, YESTERDAY, TODAY.minusHours(1))
        createQuizQuestion()
        createQuizAnswer()
        createQuestionAnswer()
        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)

        when:
        def solvedQuizDtos = statementService.getSolvedQuizzes(user.getId(), courseDto.getCourseExecutionId())

        then: 'returns correct data'
        solvedQuizDtos.size() == 1
        def solvedQuizDto = solvedQuizDtos.get(0)
        def statementQuizDto = solvedQuizDto.getStatementQuiz()
        statementQuizDto.getQuestions().size() == 1
        solvedQuizDto.statementQuiz.getAnswers().size() == 1
        def answer = solvedQuizDto.statementQuiz.getAnswers().get(0)
        answer.getSequence() == 0
        answer.getOptionId() == option.getId()
        solvedQuizDto.getCorrectAnswers().size() == 1
        def correct = solvedQuizDto.getCorrectAnswers().get(0)
        correct.getSequence() == 0
        correct.getCorrectOptionId() == option.getId()

        where:
        quizType                | conclusionDate | resultsDate
        Quiz.QuizType.PROPOSED  | YESTERDAY      | TODAY.minusHours(1)
        Quiz.QuizType.IN_CLASS  | YESTERDAY      | TODAY.minusHours(1)
        Quiz.QuizType.IN_CLASS  | YESTERDAY      | null
    }

    def "question has a private clarification request"() {
        given: "a quiz answered by the user"
        createQuiz(Quiz.QuizType.PROPOSED, YESTERDAY, TODAY.minusHours(1))
        createQuizQuestion()
        createQuizAnswer()
        createQuestionAnswer()
        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)

        and: "a clarification request about the question"
        def clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setContent('CONTENT')
        clarificationService.submitClarificationRequest(question.getId(), user.getId(), clarificationRequestDto)

        when:
        def solvedQuizDtos = statementService.getSolvedQuizzes(user.getId(), courseDto.getCourseExecutionId())

        then:
        def statementQuizDto = solvedQuizDtos.get(0).getStatementQuiz()
        statementQuizDto.getQuestions().get(0).getClarifications().size() == 0
    }

    def "question has a public clarification request"() {
        given: "a quiz answered by the user"
        createQuiz(Quiz.QuizType.PROPOSED, YESTERDAY, TODAY.minusHours(1))
        createQuizQuestion()
        createQuizAnswer()
        createQuestionAnswer()
        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)

        and: "a clarification request about the question"
        def clarificationRequestDto = new ClarificationRequestDto()
        clarificationRequestDto.setContent('CONTENT')
        clarificationRequestDto = clarificationService.submitClarificationRequest(question.getId(), user.getId(), clarificationRequestDto)
        clarificationService.changeClarificationRequestStatus(clarificationRequestDto.getId(), ClarificationRequest.RequestStatus.PUBLIC)

        when:
        def solvedQuizDtos = statementService.getSolvedQuizzes(user.getId(), courseDto.getCourseExecutionId())

        then:
        def statementQuizDto = solvedQuizDtos.get(0).getStatementQuiz()
        statementQuizDto.getQuestions().get(0).getClarifications().size() == 1
    }


    @Unroll
    def "does not return quiz with: quizType=#quizType | conclusionDate=#conclusionDate | resultsDate=#resultsDate"() {
        given: "a quiz answered by the user"
        createQuiz(quizType, conclusionDate, resultsDate)
        createQuizQuestion()
        createQuizAnswer()
        createQuestionAnswer()
        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)

        when:
        def solvedQuizDtos = statementService.getSolvedQuizzes(user.getId(), courseDto.getCourseExecutionId())

        then: 'returns no quizzes'
        solvedQuizDtos.size() == 0

        where:
        quizType                | conclusionDate | resultsDate
        Quiz.QuizType.IN_CLASS  | TOMORROW       | LATER
        Quiz.QuizType.IN_CLASS  | TOMORROW       | null
    }


    @TestConfiguration
    static class QuizServiceImplTestContextConfiguration {
        @Bean
        StatementService statementService() {
            return new StatementService()
        }
        @Bean
        AnswerService answerService() {
            return new AnswerService()
        }
        @Bean
        AnswersXmlImport answersXmlImport() {
            return new AnswersXmlImport()
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
        ClarificationService clarificationService() {
            return new ClarificationService()
        }
    }
}
