package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import spock.lang.Specification

class CreateTournamentTest extends Specification {

    def tournamentService

    def setup() {
        tournamentService = new TournamentService()
    }

    def "create a tournament"() {
        expect: false
    }

    def "create a tournament with invalid number of questions"() {
        // exception is thrown
        expect: false
    }

    def "create a tournament with available date after conclusion"() {
        // exception is thrown
        expect: false
    }
}

