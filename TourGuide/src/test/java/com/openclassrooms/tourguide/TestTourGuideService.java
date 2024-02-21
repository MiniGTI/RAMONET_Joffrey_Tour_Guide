package com.openclassrooms.tourguide;

import java.util.List;
import java.util.UUID;

import com.openclassrooms.tourguide.user.UserReward;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import tripPricer.Provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTourGuideService {
    
    @Test
    public void getUserLocationTest() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
        tourGuideService.tracker.stopTracking();
        assertTrue(visitedLocation.userId.equals(user.getUserId()));
    }
    
    @Test
    public void addUserTest() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
        
        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);
        
        User retrivedUser = tourGuideService.getUserByUsername(user.getUserName());
        User retrivedUser2 = tourGuideService.getUserByUsername(user2.getUserName());
        
        tourGuideService.tracker.stopTracking();
        
        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }
    
    @Test
    public void getAllUsersTest() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
        
        tourGuideService.addUser(user);
        tourGuideService.addUser(user2);
        
        List<User> allUsers = tourGuideService.getAllUsers();
        
        tourGuideService.tracker.stopTracking();
        
        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }
    
    @Test
    public void trackUserTest() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
        
        tourGuideService.tracker.stopTracking();
        
        assertEquals(user.getUserId(), visitedLocation.userId);
    }
    
    @Test
    public void getNearbyAttractionsTest() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
        
        List<Attraction> attractions = tourGuideService.getNearByAttractions(visitedLocation);
        
        tourGuideService.tracker.stopTracking();
        
        assertEquals(5, attractions.size());
    }
    
    @Disabled
    @Test
    public void getTripDealsTest() {
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        
        List<Provider> providers = tourGuideService.getTripDeals(user.getUserName());
        
        tourGuideService.tracker.stopTracking();
        
        assertEquals(10, providers.size());
    }

}
