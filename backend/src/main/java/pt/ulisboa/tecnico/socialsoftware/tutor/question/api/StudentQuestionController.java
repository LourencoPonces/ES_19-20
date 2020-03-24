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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentSubmitQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TeacherEvaluatesStudentQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.EvaluationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_STUDENT_QUESTION_EVALUATION;


@RestController
public class StudentQuestionController {

    @Autowired
    StudentSubmitQuestionService studentSubmitQuestionService;

    @Autowired
    CheckStudentQuestionStatusService checkStudentQuestionStatusService;

    @Autowired
    TeacherEvaluatesStudentQuestionService teacherEvaluatesStudentQuestionService;


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
     * F2: Teacher evaluates student question
     * ===========================================
     */
//
//    @GetMapping("/courses/{courseId}/studentQuestions")
//    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#courseId, 'COURSE.ACCESS')")
//    public List<StudentQuestionDTO> getCourseStudentQuestions(@PathVariable int courseId){
//
//    }

    @GetMapping("/courses/{courseId}/studentQuestions")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public List<StudentQuestionDTO> getCourseStudentQuestionsWithStatus(@PathVariable int courseId, @RequestParam Optional<StudentQuestion.SubmittedStatus> status){
        if(status.isEmpty()) {
            return teacherEvaluatesStudentQuestionService.getAllStudentQuestions(courseId);
        }
        StudentQuestion.SubmittedStatus newStatus = status.get();
        switch (newStatus) {
            case APPROVED:
                return teacherEvaluatesStudentQuestionService.getAllStudentQuestionsWithStatus(courseId, StudentQuestion.SubmittedStatus.APPROVED);
            case REJECTED:
                return teacherEvaluatesStudentQuestionService.getAllStudentQuestionsWithStatus(courseId, StudentQuestion.SubmittedStatus.REJECTED);
            case WAITING_FOR_APPROVAL:
                return teacherEvaluatesStudentQuestionService.getAllStudentQuestionsWithStatus(courseId, StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL);
            default:
                throw new TutorException(INVALID_STUDENT_QUESTION_EVALUATION);
        }
    }


    @PostMapping("/courses/{courseId}/studentQuestions/{studentQuestionId}/evaluate")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#studentQuestionId, 'QUESTION.ACCESS')")
    public StudentQuestionDTO evaluateStudentQuestion(@PathVariable int studentQuestionId, @Valid @RequestBody EvaluationDto evaluation){
        String justification = evaluation.getJustification();
        StudentQuestion.SubmittedStatus newStatus = evaluation.getEvaluation();
        switch (newStatus) {
            case APPROVED:
                return teacherEvaluatesStudentQuestionService.acceptStudentQuestion(studentQuestionId, justification);
            case REJECTED:
                return teacherEvaluatesStudentQuestionService.rejectStudentQuestion(studentQuestionId, justification);
            default:
                throw new TutorException(INVALID_STUDENT_QUESTION_EVALUATION);
        }
    }


    /* ===========================================
     * F3: Student checks questions status
     * ===========================================
     */

    @GetMapping("/courses/{courseId}/studentQuestions/checkStatus")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public List<StudentQuestionDTO> checkAllStudentQuestions(@PathVariable int courseId, @RequestParam Optional<StudentQuestion.SubmittedStatus> status, Principal principal) {

        User user = (User) ((Authentication) principal).getPrincipal();
        if(user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }
        if(status.isEmpty()) {
            return checkStudentQuestionStatusService.findByCourseAndUser(courseId, user.getId());
        }
        StudentQuestion.SubmittedStatus newStatus = status.get();
        switch (newStatus) {
            case APPROVED:
                return checkStudentQuestionStatusService.findByCourseUserAndStatus(courseId, user.getId(), StudentQuestion.SubmittedStatus.APPROVED);
            case REJECTED:
                return checkStudentQuestionStatusService.findByCourseUserAndStatus(courseId, user.getId(), StudentQuestion.SubmittedStatus.REJECTED);
            case WAITING_FOR_APPROVAL:
                return checkStudentQuestionStatusService.findByCourseUserAndStatus(courseId, user.getId(), StudentQuestion.SubmittedStatus.WAITING_FOR_APPROVAL);
            default:
                throw new TutorException(INVALID_STUDENT_QUESTION_EVALUATION);
        }
    }
}
