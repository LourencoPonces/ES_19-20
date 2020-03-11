package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

import java.util.List;

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

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto createTournament(int executionId, TournamentDto tournamentDto) {
        CourseExecution courseExecution = courseExecutionRepository.findById(executionId).orElseThrow(() -> new TutorException(COURSE_EXECUTION_NOT_FOUND, executionId));

        User creator = userRepository.findByUsername(tournamentDto.getCreator().getUsername());

        if (tournamentDto.getStatus() == null) {
            tournamentDto.setStatus(Tournament.Status.CREATED);
        }

        Tournament tournament = new Tournament(tournamentDto);

        tournamentDto.getTopics().stream().forEach(t -> {
            Topic tmp = topicRepository.findTopicByName(
                    courseExecution.getCourse().getId(),
                    t.getName());
            if (tmp == null) {
                throw new TutorException(TOPIC_NOT_FOUND, t.getId());
            } else {
                tournament.addTopic(tmp);
            }
        });

        if (!creator.getCourseExecutions().contains(courseExecution)) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, courseExecution.getAcronym());
        } else {
            tournament.setCreator(creator);
        }

        tournamentDto.setParticipants(new ArrayList<>());

        if (creator.getRole() == User.Role.STUDENT) {
            tournament.addParticipant(creator);
            tournamentDto.getParticipants().add(tournamentDto.getCreator());
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        if (tournamentDto.getCreationDate() == null) {
            LocalDateTime now = LocalDateTime.now();
            tournament.setCreationDate(now);
            tournamentDto.setCreationDate(now.format(formatter));
        } else {
            tournament.setCreationDate(LocalDateTime.parse(tournamentDto.getCreationDate(), formatter));
        }

        entityManager.persist(tournament);
        return new TournamentDto(tournament, true);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<TournamentDto> getAvailableTournaments(int executionId){
        return null;
    }
}
