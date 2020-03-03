package pt.ulisboa.tecnico.socialsoftware.tutor.studentquestion.service

import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentSubmitQuestionService
import spock.lang.Specification

class TeacherApprovesStudentQuestionTest extends Specification {

    // def studentSubmitQuestionService

    def setup() {
        // studentSubmitQuestionService = new StudentSubmitQuestionService()
    }

    def "approve teacher's question"() {
        // cannot happen: not a suggestion
        expect: false
    }

    def "approve already approved student question"() {
        // cannot happen: suggestion was already approved
        expect: false
    }

    def "approve already rejected question"() {
        // question is accepted (can be used to correct an accidental rejection)
        expect: false
    }

    def "approve existing question with no justification"() {
        // suggestion approved
        expect: false
    }

    def "approve existing question with justification"() {
        // suggestion approved
        expect: false
    }

}
