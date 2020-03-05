package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import spock.lang.Specification

class SubmitClarificationRequestServiceSpockTest extends Specification {


    def setup() {
    }

    def "the question has been answered and submit request"() {
        //the clarification request is created
        expect: false
    }

    def "same student submits 2 requests for the same question"() {
        //throw exception
        expect: false
    }

    def "the question has not been answered"() {
        //throw exception
        expect: false
    }

    def "content is empty"() {
        //throw exception
        expect: false
    }

    def "content is blank"() {
        //throw exception
        expect: false
    }
}