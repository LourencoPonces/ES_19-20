package pt.ulisboa.tecnico.socialsoftware.tutor.studentquestion.service

import org.springframework.beans.factory.annotation.Autowire
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentSubmitQuestionService
import spock.lang.Specification

class StudentSubmitQuestionTest extends Specification {

    @Autowired
    StudentSubmitQuestionService studentSubmitQuestionService

    def setup() {

    }

    def "create new suggestion with invalid input fields and submit for approval"() {

        expect: false
    }

    def "create new suggestion with invalid fields and submit for approval"() {
        expect: false
    }

    def "create new suggestion with Image and two options and 2 Topics and submit for approval"() {
        // question created and submitted
        expect: false
    }

    def "create 2 new suggestions with 1 Topic each and submit for approval"() {
        // question created and submitted
        expect: false
    }

    @TestConfiguration
    static class StudentQuestionServiceImplTestContextConfiguration {

        @Bean
        StudentSubmitQuestionService studentSubmitQuestionService() {
            return  new StudentSubmitQuestionService();
        }
    }

}


