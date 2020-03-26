package pt.ulisboa.tecnico.socialsoftware.tutor.clarification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.security.Principal;

@RestController
public class ClarificationController {

    @Autowired
    private ClarificationService clarificationService;

    @PostMapping("/student/results/questions/{questionId}/clarifications")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public ClarificationRequestDto createClarificationRequest(@PathVariable int questionId, @Valid @RequestBody ClarificationRequestDto clarificationRequestDto, Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }
        return clarificationService.submitClarificationRequest(questionId, user.getId(), clarificationRequestDto);
    }

    @PutMapping("/clarifications/{requestId}/answer")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#requestId, 'CLARIFICATION.ACCESS')")
    public ClarificationRequestAnswerDto submitAnswer(Principal principal, @PathVariable int requestId, @RequestBody String answer) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return clarificationService.submitClarificationRequestAnswer(user, requestId, answer);
    }

    @DeleteMapping("/clarifications/{requestId}/answer")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#requestId, 'CLARIFICATION.ACCESS')")
    public void deleteAnswer(Principal principal, @PathVariable int requestId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        clarificationService.deleteClarificationRequestAnswer(user, requestId);
    }

    /* ===========================================
     * F3: Student checks Clarification Request and Answer
     * ===========================================
     */

    @GetMapping("/clarifications/{requestId}/answer")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#requestId, 'CLARIFICATION.ACCESS')")
    public ClarificationRequestAnswerDto getClarificationRequestAnswer(@PathVariable int requestId, Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return clarificationService.getClarificationRequestAnswer(user.getId(), requestId);
    }
}