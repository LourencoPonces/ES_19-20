package groovy.pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import spock.lang.Specification

class GetAvailableTournamentsTest extends Specification{

    def tournamentService

    def setup() {
        tournamentService = new TournamentService()
    }

    def "get the available tournaments"() {
        expect: false
    }

    def "get the available tournaments although there are not any"() {
        // exception is thrown
        expect: false
    }
}
