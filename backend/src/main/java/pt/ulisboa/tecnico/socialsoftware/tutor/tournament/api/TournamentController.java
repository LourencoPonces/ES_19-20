package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class TournamentController {
    @Autowired
    private TournamentService tournamentService;

    @PostMapping("/executions/{executionId}/tournaments")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public TournamentDto createTournament(Principal principal, @PathVariable int executionId, @RequestBody TournamentDto tournamentDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return tournamentService.createTournament(user.getUsername(), executionId, tournamentDto);
    }

    @GetMapping("/executions/{executionId}/tournaments/available")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public List<TournamentDto> getAvailableTournaments(Principal principal, @PathVariable int executionId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }


        return tournamentService.getAvailableTournaments(executionId);
    }

    @PostMapping("/tournaments/{tournamentId}/sign-up")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#tournamentId, 'TOURNAMENT.ACCESS')")
    public void signUpInTournament(Principal principal, @PathVariable int tournamentId, @RequestBody(required = false) Optional<UserDto> userDtoOpt) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null) {
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        // FOR TESTING PURPOSES
        // Since we're unable to login as a student different from DEMO_STUDENT
        // in the Jmeter tests, we have to accept a username from the request, as
        // suggested by the professor.
        String username;
        if (userDtoOpt.isPresent() && userDtoOpt.get().getUsername() != null) {
            username = userDtoOpt.get().getUsername();
        } else {
            username = user.getUsername();
        }

        tournamentService.signUpInTournament(tournamentId, username);
    }

}
