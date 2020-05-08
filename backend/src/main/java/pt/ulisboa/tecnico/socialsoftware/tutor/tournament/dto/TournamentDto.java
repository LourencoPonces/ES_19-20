package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TournamentDto implements Serializable {
    private Integer id;
    private Integer key;
    private String title;
    private String creationDate = null;
    private String availableDate = null;
    private String runningDate = null;
    private String conclusionDate = null;
    private Integer numberOfQuestions;
    private boolean isCancelled;

    private UserDto creator;
    private List<UserDto> participants = new ArrayList<>();

    private List<TopicDto> topics = new ArrayList<>();

    public TournamentDto() {}

    public TournamentDto(Tournament tournament) {
        this.id = tournament.getId();
        this.key = tournament.getKey();
        this.title = tournament.getTitle();
        this.numberOfQuestions = tournament.getNumberOfQuestions();
        this.isCancelled = tournament.getStatus() == Tournament.Status.CANCELLED;

        if (tournament.getCreationDate() != null)
            this.creationDate = DateHandler.toISOString(tournament.getCreationDate());
        if (tournament.getAvailableDate() != null)
            this.availableDate = DateHandler.toISOString(tournament.getAvailableDate());
        if (tournament.getRunningDate() != null)
            this.runningDate = DateHandler.toISOString(tournament.getRunningDate());
        if (tournament.getConclusionDate() != null)
            this.conclusionDate = DateHandler.toISOString(tournament.getConclusionDate());

        setCreator(new UserDto(tournament.getCreator()));
        this.participants = tournament.getParticipants().stream().map(UserDto::new).collect(Collectors.toList());
        this.topics = tournament.getTopics().stream().map(TopicDto::new).collect(Collectors.toList());
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonIgnore
    public Tournament.Status getStatus() {
        if (isCancelled) return Tournament.Status.CANCELLED;

        LocalDateTime now = DateHandler.now();
        LocalDateTime creationDate = getCreationDateDate();
        LocalDateTime availableDate = getAvailableDateDate();
        LocalDateTime runningDate = getRunningDateDate();
        LocalDateTime conclusionDate = getConclusionDateDate();

        if (now.isAfter(creationDate) && now.isBefore(availableDate))
            return Tournament.Status.CREATED;
        else if (now.isAfter(availableDate) && now.isBefore(runningDate))
            return Tournament.Status.AVAILABLE;
        else if (now.isAfter(runningDate) && now.isBefore(conclusionDate))
            return Tournament.Status.RUNNING;
        else
            return Tournament.Status.FINISHED;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public UserDto getCreator() {
        return creator;
    }

    public void setCreator(UserDto creator) {
        this.creator = creator;
    }

    public List<UserDto> getParticipants() {
        return participants;
    }

    public void setParticipants(List<UserDto> participants) {
        this.participants = participants;
    }

    public List<TopicDto> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicDto> topics) {
        this.topics = topics;
    }

    public boolean getIsCancelled() { return isCancelled; }

    public void setIsCancelled() { isCancelled = true; }
    
    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(String availableDate) {
        this.availableDate = availableDate;
    }

    public String getRunningDate() {
        return runningDate;
    }

    public void setRunningDate(String runningDate) {
        this.runningDate = runningDate;
    }

    public String getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(String conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public LocalDateTime getCreationDateDate() {
        if (getCreationDate() == null || getCreationDate().isEmpty()) {
            return null;
        }
        return DateHandler.toLocalDateTime(getCreationDate());
    }

    public LocalDateTime getAvailableDateDate() {
        if (getAvailableDate() == null || getAvailableDate().isEmpty()) {
            return null;
        }
        return DateHandler.toLocalDateTime(getAvailableDate());
    }

    public LocalDateTime getRunningDateDate() {
        if (getRunningDate() == null || getRunningDate().isEmpty()) {
            return null;
        }
        return DateHandler.toLocalDateTime(getRunningDate());
    }

    public LocalDateTime getConclusionDateDate() {
        if (getConclusionDate() == null || getConclusionDate().isEmpty()) {
            return null;
        }
        return DateHandler.toLocalDateTime(getConclusionDate());
    }
}
