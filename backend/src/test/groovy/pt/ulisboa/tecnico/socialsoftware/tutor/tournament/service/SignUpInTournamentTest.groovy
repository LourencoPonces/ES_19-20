package groovy.pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import spock.lang.Specification

class SignUpInTournamentTest extends Specification{

    def tournamentService

    def setup() {
        tournamentService = new TournamentService()
    }

    def "sign-up in a tournament"() {
        expect: false
    }

    def "sign-up in a tournament although there aren't tournaments"() {
        // exception is thrown
        expect: false
    }

    def "sign-up in a tournament with available date after the current date"() {
        // exception is thrown
        expect: false
    }

    def "sign-up in a tournament with conclusion date before the current date\""() {
        // exception is thrown
        expect: false
    }

    def "sign-up in an tournament with a non-existing user"() {
        // exception is thrown
        expect: false
    }
    
}
