package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.CheckStudentQuestionStatusService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

import java.util.List;

@RestController
public class StudentQuestionController {

    @Autowired
    CheckStudentQuestionStatusService checkStudentQuestionStatusService;


//    @GetMapping("/courses/{courseId}/studentQuestions")
//    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#executionId, 'EXECUTION.ACCESS')") // TODO: check this
//    public void getAllSuggestedQuestionStatus(@PathVariable int studentQuestionID, @PathVariable int courseID) {
//        // gets all student questions
//        return;
//    }

    /* ===========================================
     * F3: Student check suggested question status
     * ===========================================
     */
    // get all suggested student questions of a given course
    @GetMapping("/courses/{courseId}/studentQuestions/{studentId}")
    @PreAuthorize("hasRole('ROLE_STUDENT')") // TODO: check this
    public List<StudentQuestionDTO> getStudentSuggestedQuestionStatus(@PathVariable int studentQuestionID, @PathVariable int courseID) {
        return checkStudentQuestionStatusService.findByCourseAndUser(studentQuestionID, courseID);
    }

    @GetMapping("/courses/{courseId}/studentQuestions/{studentId}/{status}")
    @PreAuthorize("hasRole('ROLE_STUDENT')") // TODO: check this
    public List<StudentQuestionDTO> getStudentSuggestedQuestionStatus(@PathVariable int studentQuestionID, @PathVariable int courseID, @PathVariable String status) {
        StudentQuestion.SubmittedStatus s;
        switch (status) {
            case "approved":
                s = StudentQuestion.SubmittedStatus.APPROVED;
                break;
            case "rejected":
                s = StudentQuestion.SubmittedStatus.REJECTED;
                break;
            case "pending":
                s = StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL;
                break;
            case "all":
                return checkStudentQuestionStatusService.findByCourseAndUser(studentQuestionID, courseID);
            default:
                throw new TutorException(ErrorMessage.INVALID_STATUS, status);
        }
        return checkStudentQuestionStatusService.findByCourseUserAndStatus(studentQuestionID, courseID, s);
    }
}
