package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.ClarificationRequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClarificationRequestListDto {
    private final Collection<ClarificationRequestDto> requests;

    private final Map<Integer, String> usernames = new HashMap<>();
    private final Map<Integer, String> names = new HashMap<>();

    public ClarificationRequestListDto(Collection<ClarificationRequest> requests) {
        this.requests = requests.stream()
                .map(ClarificationRequestDto::new)
                .collect(Collectors.toList());

        // attach user information to reply
        requests.stream()
                .flatMap(req -> Stream.concat(
                        Stream.of(req.getCreator()),
                        req.getMessages().stream()
                                .map(ClarificationMessage::getCreator)
                ))
                .forEach(u -> {
                    if (!names.containsKey(u.getId())) {
                        names.put(u.getId(), u.getName());
                        usernames.put(u.getId(), u.getUsername());
                    }
                });
    }

    public Map<Integer, String> getNames() {
        return names;
    }

    public Collection<ClarificationRequestDto> getRequests() {
        return requests;
    }

    public Map<Integer, String> getUsernames() {
        return usernames;
    }
}
