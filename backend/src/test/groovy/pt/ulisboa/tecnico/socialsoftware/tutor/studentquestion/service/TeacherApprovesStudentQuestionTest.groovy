package pt.ulisboa.tecnico.socialsoftware.tutor.studentquestion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentSubmitQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TeacherEvaluatesStudentQuestionService
import spock.lang.Specification

@DataJpaTest
class TeacherApprovesStudentQuestionTest extends Specification {

    @Autowired
    TeacherEvaluatesStudentQuestionService teacherEvaluatesStudentQuestionService

    def setup() {
        // criar curso
        // criar sugestao

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

    @TestConfiguration
    static class TeacherEvaluatesImplTestContextConfiguration {

        @Bean
        TeacherEvaluatesStudentQuestionService teacherEvaluatesStudentQuestionService1() {
            return  new TeacherEvaluatesStudentQuestionService();
        }
    }
}
