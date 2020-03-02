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

    def "create new question with 1 option and 1 Topic and submit for approval"() {
        // question created and submitted
        expect: false
    }

    def "create new question with Image and two options and 2 Topics and submit for approval"() {
        // question created and submitted
        expect: false
    }

    def "create 2 new questions with 1 Topic each and submit"() {
        // question created and submitted
        expect: false
    }

    def "create question without options and submit"() {
        // exception thrown
        expect: false
    }

    def "create question and without correct option and submit"() {
        // exception thrown
        expect: false
    }

    def "create question without Topics and submit"() {
        // exception thrown
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


