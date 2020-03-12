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

    def "sign-up in a tournament with CREATED status"() {
        // exception is thrown
        expect: false
    }

    def "sign-up in a tournament with RUNNING status"() {
        // exception is thrown
        expect: false
    }

    def "sign-up in an tournament with FINISHED status"() {
        // exception is thrown
        expect: false
    }

    def "sign-up in an tournament with CANCELLED status"() {
        // exception is thrown
        expect: false
    }

    def "sign-up in a tournament with a user that is already sign-up"(){
        // exception is thrown
        expect: false
    }
    
}
