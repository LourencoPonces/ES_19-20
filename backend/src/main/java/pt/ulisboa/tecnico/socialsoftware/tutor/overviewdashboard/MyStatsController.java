package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyStatsController {

    @Autowired
    private MyStatsService myStatsService;

    @GetMapping("/executions/{executionId}/dashboardStats/{username}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public MyStatsDto getMyStats(@PathVariable int executionId, @PathVariable String username) {
        return myStatsService.getMyStats(username, executionId);
    }
}
