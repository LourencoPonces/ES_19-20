package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentSubmitQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import javax.validation.Valid;

import java.security.Principal;


@RestController
public class StudentQuestionController {

    @Autowired
    StudentSubmitQuestionService studentSubmitQuestionService;

    /* ===========================================
     * F1: Student check suggested question status
     * ===========================================
     */

    @PostMapping("courses/{courseId}/studentQuestions")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public StudentQuestionDTO createStudentQuestion(@PathVariable int courseId, @Valid @RequestBody StudentQuestionDTO studentQuestion, Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        studentQuestion.setSubmittedStatus(StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL); // ensure it is pending
        return studentSubmitQuestionService.studentSubmitQuestion(courseId, studentQuestion, user.getId());
    }



    /* ===========================================
     * F3: Student check suggested question status
     * ===========================================
     */
    // get all suggested student questions of a given course
    /*@GetMapping("/courses/{courseId}/studentQuestions/{studentId}")
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
    }*/
}