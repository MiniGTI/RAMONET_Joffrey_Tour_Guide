package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.openclassrooms.tourguide.internalUser.InternalUserFactory;
import com.openclassrooms.tourguide.service.UserService;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.boot.test.context.SpringBootTest;
import com.openclassrooms.tourguide.internalUser.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import rewardCentral.RewardCentral;

@SpringBootTest
public class TestPerformance {
    
    private GpsUtil gpsUtil;
    private RewardsService rewardsService;
    private UserService userService;
    
    /*
     * A note on performance improvements:
     *
     * The number of users generated for the high volume tests can be easily
     * adjusted via this method:
     *
     * InternalTestHelper.setInternalUserNumber(100000);
     *
     *
     * These tests can be modified to suit new solutions, just as long as the
     * performance metrics at the end of the tests remains consistent.
     *
     * These are performance metrics that we are trying to hit:
     *
     * highVolumeTrackLocation: 100,000 users within 15 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     * highVolumeGetRewards: 100,000 users within 20 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */
    
    @BeforeEach
    public void setUp() {
        gpsUtil = new GpsUtil();
        rewardsService = new RewardsService(new RewardCentral());
        userService = new UserService(rewardsService);
        InternalTestHelper.setInternalUserNumber(1000);
        InternalUserFactory internalUserFactory = new InternalUserFactory(userService);
    }
    
    @Test
    public void highVolumeTrackLocation() {
        
        // Users should be incremented up to 100,000, and test finishes within 15
        // minutes
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        userService.trackAllUser();
        stopWatch.stop();
        userService.tracker.stopTracking();
        
        System.out.println(
                "highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) +
                        " seconds. " + userService.getAllUsers()
                        .size());
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }
    
    
    @Test
    public void highVolumeGetRewards() throws ExecutionException, InterruptedException {
        
        // Users should be incremented up to 100,000, and test finishes within 20
        // minutes
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        Attraction attraction = gpsUtil.getAttractions()
                .getFirst();
        List<User> allUsers = userService.getAllUsers();
        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));
        rewardsService.calculateAllUsersRewards(allUsers);

        stopWatch.stop();
        userService.tracker.stopTracking();
        
        System.out.println(
                "highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) +
                        " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }
}
