

import spock.lang.Specification

class SubmitClarificationRequestServiceSpockTest extends Specification {


    def setup() {
    }


    def "the question has not been answered"() {
        //throw exception
        expect: false
    }

    def "the question has been answered and create request"() {
        //the clarification request is created
        expect: false
    }

    def "submit a created request"() {
        //the clarification request is saved in the database
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