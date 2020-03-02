package pt.ulisboa.tecnico.socialsoftware.tutor.studentquestion.service

import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentSubmitQuestionService
import spock.lang.Specification

class TeacherRejectsStudentQuestionTest extends Specification {

    // def studentSubmitQuestionService

    def setup() {
        // studentSubmitQuestionService = new StudentSubmitQuestionService()
    }

    def "reject teacher's question"() {
        // cannot happen: not a suggestion
        expect: false
    }

    def "reject already rejected student question"() {
        // cannot happen: suggestion was already rejected
        expect: false
    }

    def "reject already accepted question"() {
        // question is rejected (can be used to correct an accidental rejection)
        // WARNING: check if question already belongs to a quizz
        expect: false
    }

    def "reject existing question with no justification"() {
        // suggestion rejected
        expect: false
    }

    def "reject existing question with justification"() {
        // suggestion rejected
        expect: false
    }
}


