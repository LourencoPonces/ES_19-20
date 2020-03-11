package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import spock.lang.Specification

class SubmitAnswerTest extends Specification {

    def "submit an answer"() {
        given: "a clarification request"

        when:
        false

        then: "the answer was submitted"
        false // TODO: impl
    }

    def "submit answer to already answered request"() {
        given: "a clarification request that already has an answer"

        when:
        false

        then: "the answer was replaced"
        false // TODO: impl
    }

    def "submit an answer with no request"() {
        when: "submitting an answer for a null clarification request"
        false

        then: "an exception"
        false // TODO: impl
    }

}
