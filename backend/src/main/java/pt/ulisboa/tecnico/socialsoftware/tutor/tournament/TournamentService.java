package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.domain.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.dto.TournamentDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import java.util.List;

@Service
public class TournamentService {

    public TournamentDto createTournament(TournamentDto tournamentDto){
        return null;
    }

    public List<TournamentDto> getAvailableTournaments(){
        return null;
    }

    public void signUpInTournament(TournamentDto tournamentDto, UserDto userDto){

    }
}
