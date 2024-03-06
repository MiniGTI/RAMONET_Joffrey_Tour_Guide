package com.openclassrooms.tourguide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.openclassrooms.tourguide.internalUser.InternalUserFactory;
import com.openclassrooms.tourguide.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.boot.test.context.SpringBootTest;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.internalUser.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

@SpringBootTest
public class TestRewardsService {

    private GpsUtil gpsUtil;
    private RewardsService rewardsService;
    @BeforeEach
    public void setUp(){
        gpsUtil = new GpsUtil();
        rewardsService = new RewardsService(new RewardCentral());
    }
    
    
    @Test
    public void userGetRewards() {
        UserService userService= new UserService(rewardsService);
        InternalTestHelper.setInternalUserNumber(0);
        
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = gpsUtil.getAttractions()
                .getFirst();
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        
        List<UserReward> userRewards = userService.getUserRewards(user);
        
        userService.tracker.stopTracking();
        
        assertEquals(1, userRewards.size());
    }
    
    @Test
    public void isWithinAttractionProximity() {
        
        Attraction attraction = gpsUtil.getAttractions()
                .getFirst();
        assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
    }
    
    @Test
    public void nearAllAttractions() {

        rewardsService.setProximityBuffer(Integer.MAX_VALUE);
        
       UserService userService= new UserService(rewardsService);
        
        InternalTestHelper.setInternalUserNumber(1);
        InternalUserFactory internalUserFactory = new InternalUserFactory(userService);
        
        List<User> users = userService.getAllUsers();

         List<UserReward> userRewards = userService.getUserRewards(users.getFirst());
        userService.tracker.stopTracking();
        
        assertEquals(gpsUtil.getAttractions()
                .size(), userRewards.size());
    }
}