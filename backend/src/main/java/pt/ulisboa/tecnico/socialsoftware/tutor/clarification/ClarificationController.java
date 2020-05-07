package pt.ulisboa.tecnico.socialsoftware.tutor.clarification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationMessageDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestListDto;
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
        User student = (User) ((Authentication) principal).getPrincipal();

        if (student == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }
        return clarificationService.submitClarificationRequest(questionId, student.getId(), clarificationRequestDto);
    }

    @DeleteMapping("/clarifications/{requestId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#requestId, 'CLARIFICATION.ACCESS')")
    public void deleteClarificationRequest(Principal principal, @PathVariable int requestId) {
        User student = (User) ((Authentication) principal).getPrincipal();

        if (student == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        clarificationService.deleteClarificationRequest(student.getId(), requestId);
    }

    @GetMapping("/clarifications")
    @PreAuthorize("hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')")
    public ClarificationRequestListDto getUserClarificationRequests(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        if (user.getRole() == User.Role.TEACHER) {
            return clarificationService.getTeacherClarificationRequests(user.getId());
        } else if (user.getRole() == User.Role.STUDENT) {
            return clarificationService.getStudentClarificationRequests(user.getId());
        } else {
            throw new TutorException(ErrorMessage.ACCESS_DENIED);
        }
    }

    @PostMapping("/clarifications/{requestId}/messages")
    @PreAuthorize("(hasRole('ROLE_TEACHER') or hasRole('ROLE_STUDENT')) and hasPermission(#requestId, 'CLARIFICATION.ACCESS')")
    public ClarificationMessageDto submitMessage(Principal principal, @PathVariable int requestId, @RequestBody ClarificationMessageDto messageDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return clarificationService.submitClarificationMessage(user.getId(), requestId, messageDto);
    }

    @DeleteMapping("/clarifications/messages/{messageId}")
    public void deleteClarificationMessage(@PathVariable int messageId, Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        clarificationService.deleteClarificationMessage(user.getId(), messageId);
    }

    @PutMapping("/clarifications/{requestId}/status")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#requestId, 'CLARIFICATION.ACCESS')")
    public ClarificationRequestDto changeClarificationRequestStatus(Principal principal, @PathVariable int requestId, @RequestBody String status) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return clarificationService.changeClarificationRequestStatus(requestId, ClarificationRequest.RequestStatus.valueOf(status));
    }
}
