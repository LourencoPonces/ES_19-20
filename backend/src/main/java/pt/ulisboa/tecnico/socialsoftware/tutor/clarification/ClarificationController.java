package pt.ulisboa.tecnico.socialsoftware.tutor.clarification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.security.Principal;

@RestController
public class ClarificationController {

    @Autowired
    private ClarificationService clarificationService;

    @PutMapping("/clarifications/{requestId}/answer")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#requestId, 'CLARIFICATION.SUBMIT_ANSWER')")
    public ClarificationRequestAnswerDto submitAnswer(Principal principal, @PathVariable int requestId, @RequestBody String answer) {
        User user = (User) ((Authentication) principal).getPrincipal();

        return clarificationService.submitClarificationRequestAnswer(user, requestId, answer);
    }

    @DeleteMapping("/clarifications/{requestId}/answer")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#requestId, 'CLARIFICATION.DELETE_ANSWER')")
    public void deleteAnswer(Principal principal, @PathVariable int requestId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        clarificationService.deleteClarificationRequestAnswer(user, requestId);
    }

}