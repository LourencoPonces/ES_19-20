

import spock.lang.Specification

class SubmitClarificationRequestServiceSpockTest extends Specification {


    def setup() {
    }

    def "the question has been answered and submit request"() {
        //the clarification request is created/submitted
        expect: false
    }

    def "the question has not been answered"() {
        //throw exception
        expect: false
    }

    def "question is null"() {
        //throw exception
        expect: false
    }

    def "student is null"() {
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