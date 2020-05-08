package pt.ulisboa.tecnico.socialsoftware.tutor.overviewdashboard;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.security.Principal;

@RestController
public class MyStatsController {

    @Autowired
    private MyStatsService myStatsService;

    @GetMapping("/courses/{courseId}/dashboardStats/{userId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public MyStatsDto getDashboardStats(@PathVariable int courseId, @PathVariable int userId, Principal principal) {
        User loggedInUser = (User) ((Authentication) principal).getPrincipal();

        if (loggedInUser.getId() == userId) {
            return myStatsService.getMyStats(loggedInUser.getId(), courseId);
        } else {
            return this.myStatsService.getOtherUserStats(userId, courseId);
        }
    }

    @PutMapping("/dashboardStats/{statsId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#statsId, 'STATS.ACCESS')")
    public MyStatsDto updateDashboardStatsVisibility(@PathVariable int statsId, @RequestBody MyStatsDto myStatsDto) {
        return this.myStatsService.updateVisibility(statsId, myStatsDto);
    }
}
