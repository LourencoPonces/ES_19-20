package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentSubmitQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TeacherEvaluatesStudentQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDTO;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;;

import javax.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_STUDENT_QUESTION_EVALUATION;


@RestController
public class StudentQuestionController {

    @Autowired
    StudentSubmitQuestionService studentSubmitQuestionService;

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
        return studentSubmitQuestionService.studentSubmitQuestion(courseId, studentQuestion, user.getId());
    }



    /* ===========================================
     * F2: Teacher evaluates student question
     * ===========================================
     */

    @GetMapping("/courses/{courseId}/studentQuestions")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public List<StudentQuestionDTO> getCourseStudentQuestions(@PathVariable int courseId){
        return teacherEvaluatesStudentQuestionService.getAllStudentQuestions(courseId);
    }

    @PutMapping("/courses/{courseId}/studentQuestions/{studentQuestionId}/{evaluation}")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public StudentQuestionDTO evaluateStudentQuestion(@PathVariable int studentQuestionId, @PathVariable String evaluation, @Valid @RequestBody String justification){
        StudentQuestion.SubmittedStatus newStatus = StudentQuestion.SubmittedStatus.valueOf(evaluation);
        switch (newStatus) {
            case APPROVED:
                return teacherEvaluatesStudentQuestionService.acceptStudentQuestion(studentQuestionId, justification);
            case REJECTED:
                return teacherEvaluatesStudentQuestionService.rejectStudentQuestion(studentQuestionId, justification);
            default:
                throw new TutorException(INVALID_STUDENT_QUESTION_EVALUATION);
        }
    }

}