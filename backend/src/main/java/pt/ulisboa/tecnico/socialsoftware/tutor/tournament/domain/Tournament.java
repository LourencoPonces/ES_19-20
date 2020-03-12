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

        setDates(tournamentDto);

        int numQuestions = tournamentDto.getNumberOfQuestions();
        checkNumberOfQuestions(numQuestions);
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
        checkCreationDate(creationDate);
        this.creationDate = creationDate;
    }

    public LocalDateTime getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(LocalDateTime availableDate) {
        checkAvailableDate(availableDate);
        this.availableDate = availableDate;
        updateStatus();
    }

    public LocalDateTime getRunningDate() {
        return runningDate;
    }

    public void setRunningDate(LocalDateTime runningDate) {
        checkRunningDate(runningDate);
        this.runningDate = runningDate;
        updateStatus();
    }

    public LocalDateTime getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(LocalDateTime conclusionDate) {
        checkConclusionDate(conclusionDate);
        this.conclusionDate = conclusionDate;
        updateStatus();
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

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        checkNumberOfQuestions(numberOfQuestions);
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

    public void cancel(){
        status = Status.CANCELLED;
    }

    @Override
    public String toString() {
        // TODO
        return "Tournament{" +
                "id=" + id +
                ", creationDate=" + creationDate +
                ", availableDate=" + availableDate +
                ", runningDate=" + runningDate +
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

    private void checkCreationDate(LocalDateTime creationDate) {
        if (!(creationDate != null
                && (creationDate.isBefore(getAvailableDate()) || creationDate.isEqual(getAvailableDate())))) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Creation date");
        }
    }

    private void checkAvailableDate(LocalDateTime availableDate) {
        if (!(availableDate != null
                && (availableDate.isEqual(getCreationDate()) || availableDate.isAfter(getCreationDate()))
                && availableDate.isBefore(getRunningDate()))) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Available date");
        }
    }

    private void checkRunningDate(LocalDateTime runningDate) {
        if (!(runningDate != null
                && runningDate.isAfter(getAvailableDate())
                && runningDate.isBefore(getConclusionDate()))) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Running date");
        }
    }

    private void checkConclusionDate(LocalDateTime conclusionDate) {
        if (!(conclusionDate != null
                && conclusionDate.isAfter(getRunningDate()))) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Conclusion date");
        }
    }

    private void checkNumberOfQuestions(Integer numberOfQuestions) {
        if (numberOfQuestions == null || numberOfQuestions < 1) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, numberOfQuestions);
        }
    }

    private void setDates(TournamentDto tournamentDto) {
        this.creationDate = tournamentDto.getCreationDateDate();
        this.availableDate = tournamentDto.getAvailableDateDate();
        this.runningDate = tournamentDto.getRunningDateDate();
        this.conclusionDate = tournamentDto.getConclusionDateDate();

        checkCreationDate(this.creationDate);
        checkAvailableDate(this.availableDate);
        checkRunningDate(this.runningDate);
        checkConclusionDate(this.conclusionDate);

        updateStatus();
    }

    private void updateStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(creationDate) && now.isBefore(availableDate))
            status = Status.CREATED;
        else if (now.isAfter(availableDate) && now.isBefore(runningDate))
            status = Status.AVAILABLE;
        else if (now.isAfter(runningDate) && now.isBefore(conclusionDate))
            status = Status.RUNNING;
        else
            status = Status.FINISHED;
    }

    //TODO: Not needed
    /*
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
    }*/

}
