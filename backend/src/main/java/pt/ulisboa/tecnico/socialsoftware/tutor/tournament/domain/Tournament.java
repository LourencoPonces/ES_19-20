package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_CONSISTENT;

@Entity
@Table(name = "tournaments")
public class Tournament {
    public enum Status {CREATED, AVAILABLE, RUNNING, FINISHED, CANCELLED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique=true, nullable = false)
    private Integer key;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "available_date")
    private LocalDateTime availableDate;

    @Column(name = "running_date")
    private LocalDateTime runningDate;

    @Column(name = "conclusion_date")
    private LocalDateTime conclusionDate;

    @Column(nullable = false)
    private String title = "Title";

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "number_of_questions")
    private Integer numberOfQuestions;

    @ManyToMany(mappedBy = "tournaments")
    private Set<Topic> topics = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "course_execution_id")
    private CourseExecution courseExecution;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToMany(mappedBy = "tournaments")
    private Set<User> participants = new HashSet<>();

    public Tournament() {}

    public Tournament(TournamentDto tournamentDto) {
        this.key = tournamentDto.getKey();
        setTitle(tournamentDto.getTitle());
        this.creationDate = tournamentDto.getCreationDateDate();
        setConclusionDate(tournamentDto.getConclusionDateDate());
        setRunningDate(tournamentDto.getRunningDateDate());
        setAvailableDate(tournamentDto.getAvailableDateDate());

        setStatus(tournamentDto.getStatus());

        int numQuestions = tournamentDto.getNumberOfQuestions();
        if (numQuestions <= 0) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, numQuestions);
        }
        this.numberOfQuestions = numQuestions;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(LocalDateTime availableDate) {
        checkAvailableDate(availableDate);
        this.availableDate = availableDate;
    }

    public LocalDateTime getRunningDate() {
        return runningDate;
    }

    public void setRunningDate(LocalDateTime runningDate) {
        checkRunningDate(runningDate);
        this.runningDate = runningDate;
    }

    public LocalDateTime getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(LocalDateTime conclusionDate) {
        checkConclusionDate(conclusionDate);
        this.conclusionDate = conclusionDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        checkTitle(title);
        this.title = title;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        checkStatus(status);
        this.status = status;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public Set<Topic> getTopics() {
        return topics;
    }

    public void addTopic(Topic topic) {
        this.topics.add(topic);
    }

    public CourseExecution getCourseExecution() {
        return courseExecution;
    }

    public void setCourseExecution(CourseExecution courseExecution) {
        this.courseExecution = courseExecution;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void addParticipant(User participant) {
        this.participants.add(participant);
    }

    @Override
    public String toString() {
        // TODO
        return "Tournament{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", availableDate=" + availableDate +
                ", conclusionDate=" + conclusionDate +
                ", title='" + title + '\'' +
                ", status=" + status +
                '}';
    }

    private void checkTitle(String title) {
        if (title == null || title.trim().length() == 0) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Title");
        }
    }

    private void checkAvailableDate(LocalDateTime availableDate) {
        if (!(availableDate != null
                && creationDate != null
                && (creationDate.isBefore(availableDate) || creationDate.isEqual(availableDate))
                && availableDate.isBefore(getRunningDate())
                && availableDate.isBefore(getConclusionDate()))) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Available date");
        }
    }

    private void checkRunningDate(LocalDateTime runningDate) {
        if (!(runningDate != null
                && runningDate.isBefore(getConclusionDate()))) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Running date");
        }
    }

    private void checkConclusionDate(LocalDateTime conclusionDate) {
        if (conclusionDate == null) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Conclusion date");
        }
    }

    private void checkStatus(Status status) {
        LocalDateTime now = LocalDateTime.now();
        if (!((status == Status.CREATED
                && (now.isEqual(creationDate)   || now.isAfter(creationDate))
                && (now.isEqual(availableDate)  || now.isBefore(availableDate)))

                || (status == Status.AVAILABLE
                && (now.isEqual(availableDate)  || now.isAfter(availableDate))
                && (now.isEqual(runningDate)    || now.isBefore(runningDate)))

                || (status == Status.RUNNING
                && (now.isEqual(runningDate)    || now.isAfter(runningDate))
                && (now.isEqual(conclusionDate) || now.isBefore(conclusionDate)))

                || (status == Status.FINISHED
                && (now.isEqual(conclusionDate) || now.isAfter(conclusionDate)))

                || status == Status.CANCELLED
        )) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "State");
        }
    }
}
