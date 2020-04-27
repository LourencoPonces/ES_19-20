package pt.ulisboa.tecnico.socialsoftware.tutor.clarification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.ClarificationRequestDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

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

    @DeleteMapping("/student/clarifications/{requestId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#requestId, 'CLARIFICATION.ACCESS')")
    public void deleteClarificationRequest(Principal principal, @PathVariable int requestId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        clarificationService.deleteClarificationRequest(user.getId(), requestId);
    }

    @PostMapping("/student/clarifications/{requestId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#requestId, 'CLARIFICATION.ACCESS')")
    public ClarificationRequestDto updateClarificationRequest(Principal principal,  @PathVariable int requestId, @Valid @RequestBody ClarificationRequestDto clarificationRequestDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return clarificationService.updateClarificationRequest(clarificationRequestDto);
    }

    @GetMapping("/student/clarifications")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public List<ClarificationRequestDto> getStudentClarificationRequests(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return clarificationService.getStudentClarificationRequests(user.getId());
    }

    @GetMapping("/clarifications")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public List<ClarificationRequestDto> getTeacherClarificationRequests(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return clarificationService.getTeacherClarificationRequests(user.getId());
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


    @GetMapping("/clarifications/{requestId}/answer")
    @PreAuthorize("(hasRole('ROLE_STUDENT') or hasRole('ROLE_TEACHER')) and hasPermission(#requestId, 'CLARIFICATION.ACCESS')")
    public ClarificationRequestAnswerDto getClarificationRequestAnswer(@PathVariable int requestId, Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return clarificationService.getClarificationRequestAnswer(user.getId(), requestId);
    }

    @PutMapping("/clarifications/{requestId}/status")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#requestId, 'CLARIFICATION.ACCESS')")
    public ClarificationRequestDto changeClarificationRequestStatus(Principal principal, @PathVariable int requestId, @RequestBody ClarificationRequest.RequestStatus status) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(ErrorMessage.AUTHENTICATION_ERROR);
        }

        return clarificationService.changeClarificationRequestStatus(requestId, status);
    }
}
