package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class CheckClarificationRequestAnswerSpockTest extends Specification {

    def "the student submitted the request, receives the answer"() {
        // the correct answer is returned
        expect: false
    }

    def "the student didn't submit a clarification request for the question"() {
        // throw exception
        expect: false
    }

    def "there is no answer available"() {
        // throw exception
        expect: false
    }

    def "the question doesn't exist"() {
        //throw exception
        expect: false
    }

    def "the user isn't a student"() {
        //throw exception
        expect: false
    }
}