package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.repository.TournamentRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.util.*;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TournamentService {
    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizService quizService;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto createTournament(String username, int executionId, TournamentDto tournamentDto) {
        CourseExecution courseExecution = getCourseExecution(executionId);

        User creator = userRepository.findByUsername(username);

        checkKey(tournamentDto);

        Tournament tournament = new Tournament(tournamentDto);

        checkTopics(tournamentDto, courseExecution, tournament);

        checkCreatorCourseExecution(courseExecution, creator, tournament);

        tournamentDto.setParticipants(new ArrayList<>());

        addCreator(tournamentDto, creator, tournament);

        tournament.setCourseExecution(courseExecution);
        entityManager.persist(tournament);

        creator.addCreatedTournament(tournament);
        creator.addParticipantTournament(tournament);
        entityManager.persist(creator);

        addTournamentToTopics(tournamentDto, courseExecution, tournament);

        courseExecution.addTournament(tournament);
        entityManager.persist(courseExecution);

        return new TournamentDto(tournament);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<TournamentDto> getAvailableTournaments(int executionId) {
        getCourseExecution(executionId);

        List<TournamentDto> availableTournaments = tournamentRepository.findAvailableTournaments(executionId).stream().map(TournamentDto::new).collect(Collectors.toList());

        return availableTournaments;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void signUpInTournament(int tournamentId, String username){
        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentId));

        User user = userRepository.findByUsername(username);

        if (tournament.getStatus() != Tournament.Status.AVAILABLE) {
            throw new TutorException(TOURNAMENT_NOT_AVAILABLE);
        }

        if (tournament.getParticipants().contains(user)) {
            throw new TutorException(USER_ALREADY_SIGNED_UP_IN_TOURNAMENT);
        }

        tournament.addParticipant(user);
        user.addParticipantTournament(tournament);

        if (tournament.getQuiz() == null) {
            if (tournament.getParticipants().size() == 2)
                generateQuiz(tournament);
        } else {
            entityManager.persist(new QuizAnswer(user, tournament.getQuiz()));
        }

        entityManager.persist(tournament);
        entityManager.persist(user);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void deleteTournament(String username, int tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentId));

        if (!tournament.getCreator().getUsername().equals(username)) {
            throw new TutorException(MISSING_TOURNAMENT_OWNERSHIP);
        }
        tournament.delete();
        tournamentRepository.deleteById(tournamentId);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CourseDto findTournamentCourseExecution(int tournamentId) {
        return this.tournamentRepository.findById(tournamentId)
                .map(Tournament::getCourseExecution)
                .map(CourseDto::new)
                .orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentId));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<TournamentDto> getCreatedTournaments(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        return user.getCreatedTournaments()
                .stream()
                .map(TournamentDto::new)
                .sorted(Comparator.comparing(TournamentDto::getCreationDateDate).reversed())
                .collect(Collectors.toList());
    }

    private void checkKey(TournamentDto tournamentDto) {
        if (tournamentDto.getKey() == null) {
            int maxQuestionNumber = tournamentRepository.getMaxTournamentKey() != null ?
                    tournamentRepository.getMaxTournamentKey() : 0;
            tournamentDto.setKey(maxQuestionNumber + 1);
        }
    }

    private void addCreator(TournamentDto tournamentDto, User creator, Tournament tournament) {
        if (creator.getRole() == User.Role.STUDENT) {
            tournament.setCreator(creator);
            tournament.addParticipant(creator);
            tournamentDto.getParticipants().add(tournamentDto.getCreator());
        } else {
            throw new TutorException(TOURNAMENT_CREATED_BY_NON_STUDENT);
        }
    }

    private void checkCreatorCourseExecution(CourseExecution courseExecution, User creator, Tournament tournament) {
        if (!creator.getCourseExecutions().contains(courseExecution)) {
            throw new TutorException(USER_NOT_ENROLLED_IN_COURSE_EXECUTION, courseExecution.getAcronym());
        }
    }

    private void checkTopics(TournamentDto tournamentDto, CourseExecution courseExecution, Tournament tournament) {
        for(TopicDto t: tournamentDto.getTopics()) {
            Topic topic = topicRepository.findTopicByName(courseExecution.getCourse().getId(), t.getName());
            if (topic == null) {
                throw new TutorException(TOPIC_NOT_FOUND, t.getId());
            } else {
                tournament.addTopic(topic);
            }
        }
    }

    private CourseExecution getCourseExecution(int executionId) {
        return courseExecutionRepository.findById(executionId).orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, executionId));
    }

    private void addTournamentToTopics(TournamentDto tournamentDto, CourseExecution courseExecution, Tournament tournament){
        for(TopicDto t: tournamentDto.getTopics()) {
            Topic topic = topicRepository.findTopicByName(courseExecution.getCourse().getId(), t.getName());
            topic.addTournament(tournament);
            entityManager.persist(topic);
        }
    }

    private void generateQuiz(Tournament tournament) {
        Quiz quiz = new Quiz();

        quiz.setKey(quizService.getMaxQuizKey() + 1);
        quiz.setType(Quiz.QuizType.TOURNAMENT.toString());

        quiz.setCreationDate(tournament.getCreationDate());
        quiz.setAvailableDate(tournament.getRunningDate());
        quiz.setConclusionDate(tournament.getConclusionDate());
        quiz.setResultsDate(tournament.getConclusionDate());

        entityManager.persist(quiz);

        // Add quiz answers to the first two signed-up students
        for (User user: tournament.getParticipants()) {
            QuizAnswer quizAnswer = new QuizAnswer(user, quiz);
            entityManager.persist(quizAnswer);
        }

        ArrayList<Question> validQuestions = getValidQuestions(tournament);
        Random rand = new Random(System.currentTimeMillis());

        int numQuestions = tournament.getNumberOfQuestions();

        while (quiz.getQuizQuestions().size() < numQuestions) {
            int next = rand.nextInt(numQuestions);
            if (!quiz.getQuizQuestions().contains(validQuestions.get(next))) {
                quiz.getQuizQuestions().add(new QuizQuestion(quiz, validQuestions.get(next),
                        quiz.getQuizQuestions().size()));
            }
        }

        tournament.setQuiz(quiz);
    }

    private ArrayList<Question> getValidQuestions(Tournament tournament) {
        List<Question> availableQuestions = questionRepository.findAvailableQuestions(
                tournament.getCourseExecution().getCourse().getId());

        ArrayList<Question> validQuestions = new ArrayList<>();

        Set<Topic> topics = tournament.getTopics();

        // Only accept questions whose every topic belongs to the tournament
        for (Question question: availableQuestions) {
            boolean valid = true;

            for (Topic questionTopic: question.getTopics()) {
                if (!topics.contains(questionTopic)) {
                    valid = false;
                    break;
                }
            }

            if (valid) validQuestions.add(question);
        }

        return validQuestions;
    }
}
