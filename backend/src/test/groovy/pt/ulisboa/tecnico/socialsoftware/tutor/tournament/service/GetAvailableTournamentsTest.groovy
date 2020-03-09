package groovy.pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto
import spock.lang.Specification

import java.time.format.DateTimeFormatter

class GetAvailableTournamentsTest extends Specification{
    public static final Integer TOURNAMENT_KEY = 1
    public static final String TOURNAMENT_TITLE = "First Tournament"
    public static final Integer NUMBER_OF_QUESTIONS = 5

    @Autowired
    def tournamentService

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }

    def "get the available tournaments"() {
        given: "a tournamentDto"
        def tournamentDto = new TournamentDto()
        tournamentDto.setKey(TOURNAMENT_KEY)
        tournamentDto.setTitle(TOURNAMENT_TITLE)
        tournamentDto.setStatus(Tournament.Status.AVAILABLE.name())
        tournamentDto.setNumberOfQuestions(NUMBER_OF_QUESTIONS)
        and:
        tournamentService.createTournament(tournamentDto)

        when:
        def tournamentDtos = tournamentService.getAvailableTournaments()

        then: "the return data are correct"
        tournamentDtos.size() == 1
        def tournamentElement = tournamentDtos.get(0)
        tournamentElement.getKey() == TOURNAMENT_KEY
        tournamentElement.getTitle().equals(TOURNAMENT_TITLE)
        tournamentElement.getStatus == Tournament.Status.AVAILABLE
        tournamentElement.getNumberOfQuestions() == NUMBER_OF_QUESTIONS
        tournamentElement.getCreationDateDate() == null
        tournamentElement.getAvailableDateDate() == null
        tournamentElement.getConclusionDateDate() == null
    }

    def "get the available tournaments although there are not any"() {
        // exception is thrown
        expect: false
    }
}
