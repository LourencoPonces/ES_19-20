package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.security.Principal;

@RestController
public class MyStatsController {

    @Autowired
    private MyStatsService myStatsService;

    @GetMapping("/courses/{courseId}/dashboardStats/{username}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public MyStatsDto getDashboardStats(@PathVariable int courseId, @PathVariable String username, Principal principal) {
        User loggedInUser = (User) ((Authentication) principal).getPrincipal();

        if(loggedInUser.getUsername().equals(username)) {
            return myStatsService.getMyStats(loggedInUser.getUsername(), courseId);
        } else {
            return myStatsService.getOtherUserStats(username, courseId);
        }
    }
}
