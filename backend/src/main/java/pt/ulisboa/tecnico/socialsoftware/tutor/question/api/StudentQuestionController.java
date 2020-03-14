package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.CheckStudentQuestionStatusService;

@RestController
public class StudentQuestionController {

    @Autowired
    CheckStudentQuestionStatusService checkStudentQuestionStatusService;

    @GetMapping("/courses/{courseId}/studentQuestions")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public void getAllSuggestedQuestionStatus(@PathVariable int studentQuestionID, @PathVariable int courseID) {
        // gets all student questions
        return;
    }

    @GetMapping("/courses/{courseId}/studentQuestions/{studentId}")
    @PreAuthorize("hasRole('ROLE_STUDENT')") // TODO: check this
    public void getStudentSuggestedQuestionStatus(@PathVariable int studentQuestionID, @PathVariable int courseID) {
        // teacher wants to see suggested questions by student {studentId}: future feature
        // student wants to see his questions
        // get all submitted questions
        return;
    }
}
