package pt.ulisboa.tecnico.socialsoftware.tutor.studentquestion.service

import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentSubmitQuestionService
import spock.lang.Specification

class StudentSubmitQuestionTest extends Specification {

    def studentSubmitQuestionService

    def setup() {
        studentSubmitQuestionService = new StudentSubmitQuestionService()
    }

    def "create new question with 1 option and 1 Topic and submit for approval"() {
        // question created and submitted
        expect: false
    }

    def "create new question with Image and two options and 2 Topics and submit for approval"() {
        // question created and submitted
        expect: false
    }

    def "create 2 new questions with 1 Topic each"() {
        // question created and submitted
        expect: false
    }

    def "create question without options"() {
        // exception thrown
        expect: false
    }

    def "create question without Topics"() {
        // exception thrown
        expect: false
    }

}


