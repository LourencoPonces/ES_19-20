package pt.ulisboa.tecnico.socialsoftware.tutor.studentquestion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentCheckQuestionStatusService
import spock.lang.Specification

class StudentCheckStatusTest extends Specification {

    @Autowired
    StudentCheckQuestionStatusService studentCheckQuestionStatusService

    def "check status of existing suggestion that is accepted"() {
        // return question status
        expect: false
    }

    def "check status of existing suggestion that is rejected"() {
        // return question status
        expect: false
    }

    def "check status of existing suggestion that is waiting revision"() {
        // return question status
        expect: false
    }

    def "check suggestion status of different user"() {
        // exception thrown
        expect: false
    }

    def "check status of non exiting suggestion"() {
        // exception thrown
        expect: false
    }



    @TestConfiguration
    static class StudentCheckQuestionImplTestContxtConfiguration {

        @Bean
        StudentCheckQuestionStatusService studentCheckQuestionStatusService() {
            return new StudentCheckQuestionStatusService();
        }
    }
}
