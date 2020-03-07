package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import spock.lang.Specification

class RemoveAnswerTest extends Specification {

    def "remove an answer"() {
        given: "answered clarification request"

        when: "answer is removed"

        expect: "clarification request has no answer"
        false
    }

    def "null checks"() {
        expect: "thrown exception"
        false
    }

}
