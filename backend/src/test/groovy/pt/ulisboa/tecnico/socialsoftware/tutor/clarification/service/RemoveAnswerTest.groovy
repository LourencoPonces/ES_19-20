package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class RemoveAnswerTest extends Specification {

    def "remove an answer"() {
        given: "answered clarification request"

        when: "answer is removed"
        false

        then: "clarification request has no answer"
        false
    }

    def "null checks"() {
        expect: "thrown exception"
        false
    }

}
