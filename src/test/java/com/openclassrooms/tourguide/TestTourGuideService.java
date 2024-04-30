package com.openclassrooms.tourguide;

import java.util.List;
import java.util.UUID;

import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.service.UserService;
import gpsUtil.GpsUtil;
import org.junit.jupiter.api.Test;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.boot.test.context.SpringBootTest;
import com.openclassrooms.tourguide.internalUser.InternalTestHelper;
import com.openclassrooms.tourguide.user.User;
import rewardCentral.RewardCentral;
import tripPricer.Provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TestTourGuideService {

    
    @Test
    public void getUserLocationTest() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        UserService userService= new UserService(rewardsService);
        
        
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = userService.trackUserLocation(user);
        userService.tracker.stopTracking();
       
        assertTrue(visitedLocation.userId.equals(user.getUserId()));
        
    }
    
    @Test
    public void addUserTest() {
        InternalTestHelper.setInternalUserNumber(0);
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(new RewardCentral());
        UserService userService= new UserService(rewardsService);
        
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
        
        userService.addUser(user);
        userService.addUser(user2);
        
        User retrivedUser = userService.getUserByUsername(user.getUserName());
        User retrivedUser2 = userService.getUserByUsername(user2.getUserName());
        
        userService.tracker.stopTracking();
        
        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }
    
    @Test
    public void getAllUsersTest() {
        InternalTestHelper.setInternalUserNumber(0);
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(new RewardCentral());
        UserService userService= new UserService(rewardsService);
        
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
        
        userService.addUser(user);
        userService.addUser(user2);
        
        List<User> allUsers = userService.getAllUsers();
        
        userService.tracker.stopTracking();
        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }
    
    @Test
    public void trackUserTest() {
        InternalTestHelper.setInternalUserNumber(0);
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(new RewardCentral());
        UserService userService= new UserService(rewardsService);
        
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = userService.trackUserLocation(user);
        
        userService.tracker.stopTracking();
        
        assertEquals(user.getUserId(), visitedLocation.userId);
    }
    
    @Test
    public void getNearbyAttractionsTest() {
        InternalTestHelper.setInternalUserNumber(0);
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(new RewardCentral());
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        UserService userService= new UserService(rewardsService);
        
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        
        VisitedLocation visitedLocation = userService.trackUserLocation(user);
        
        List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);
        
        userService.tracker.stopTracking();
        
        assertEquals(5, attractions.size());
    }
    

    @Test
    public void getTripDealsTest() {
        InternalTestHelper.setInternalUserNumber(0);
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(new RewardCentral());
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        UserService userService= new UserService(rewardsService);
        
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        userService.addUser(user);
        List<Provider> providers = tourGuideService.getTripDeals(user);
        
        userService.tracker.stopTracking();
        
        assertEquals(5, providers.size());
    }
    
}