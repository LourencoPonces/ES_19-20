package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.ClarificationService;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import java.sql.SQLException;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class MyStatsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    MyStatsRepository myStatsRepository;

    @Autowired
    private ClarificationService clarificationService;


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public MyStatsDto getMyStats(int userId, int courseId) {
        User user = validateUserAndCourse(userId, courseId);

        MyStatsDto statsDto = new MyStatsDto(user.getMyStats());

        //If it's the logged in user, calculate the stats independently of visibility
        statsDto.setRequestsSubmittedStat(calculateRequestsSubmitted(user, courseId));
        statsDto.setPublicRequestsStat(calculatePublicRequests(user, courseId));
        statsDto.setSubmittedQuestionsStat(calculateSubmittedQuestions(user, courseId));
        statsDto.setApprovedQuestionsStat(calculateApprovedQuestions(user, courseId));

        return statsDto;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public MyStatsDto getOtherUserStats(int userId, int courseId) {
        User user = validateUserAndCourse(userId, courseId);

        MyStatsDto statsDto = new MyStatsDto(user.getMyStats());

        if (user.getMyStats().canSeeRequestsSubmitted())
            statsDto.setRequestsSubmittedStat(calculateRequestsSubmitted(user, courseId));

        if (user.getMyStats().canSeePublicRequests())
            statsDto.setPublicRequestsStat(calculatePublicRequests(user, courseId));

        if (user.getMyStats().canSeeSubmittedQuestions())
            statsDto.setSubmittedQuestionsStat(calculateSubmittedQuestions(user, courseId));

        if(user.getMyStats().canSeeApprovedQuestions())
            statsDto.setApprovedQuestionsStat(calculateApprovedQuestions(user, courseId));
        
        return statsDto;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public MyStatsDto updateVisibility(Integer myStatsId, MyStatsDto myStatsDto) {
        MyStats myStats = myStatsRepository.findById(myStatsId).orElseThrow(() -> new TutorException(NO_MY_STATS_FOUND, myStatsId));
        myStats.updateVisibility(myStatsDto);
        return new MyStatsDto(myStats, myStatsDto);
    }

    private User validateUserAndCourse(int userId, int courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, userId));

        if (!courseRepository.existsById(courseId)) {
            throw new TutorException(COURSE_NOT_FOUND);
        }
        return user;
    }

    private Integer calculateRequestsSubmitted(User user, int courseId) {
        return (int) user.getClarificationRequests()
                .stream()
                .filter(req -> clarificationService.findClarificationRequestCourseId(req.getId()) == courseId)
                .count();
    }

    private Integer calculatePublicRequests(User user, int courseId) {
        return (int) user.getClarificationRequests()
                .stream()
                .filter(req ->
                        clarificationService.findClarificationRequestCourseId(req.getId()) == courseId &&
                        req.getStatus() == ClarificationRequest.RequestStatus.PUBLIC)
                .count() ;
    }

    private Integer calculateSubmittedQuestions(User user, int courseId) {
        return (int) user.getStudentQuestions()
                .stream()
                .filter(sq -> sq.getCourse().getId() == courseId)
                .count();
    }

    private Integer calculateApprovedQuestions(User user, int courseId) {
        return (int) user.getStudentQuestions()
                .stream()
                .filter(sq -> sq.getCourse().getId() == courseId &&
                        (sq.getSubmittedStatus() == StudentQuestion.SubmittedStatus.APPROVED) // TODO: Add promoted
                ).count();
    }

    public Integer findOwner(int statsId) {
        return myStatsRepository.findById(statsId)
                .map(stats -> stats.getUser().getId())
                .orElse(-1);
    }


}
