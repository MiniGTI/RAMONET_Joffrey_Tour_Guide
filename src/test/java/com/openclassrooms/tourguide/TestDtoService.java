package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.dto.NearAttractionsListDto;
import com.openclassrooms.tourguide.dto.TripDealsDto;
import com.openclassrooms.tourguide.dto.UserLocationDto;
import com.openclassrooms.tourguide.dto.UserRewardsDto;
import com.openclassrooms.tourguide.internalUser.InternalTestHelper;
import com.openclassrooms.tourguide.service.DtoService;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserPreferences;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import rewardCentral.RewardCentral;
import tripPricer.TripPricer;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TestDtoService {
    
    
    private TourGuideService tourGuideService;
    
    private RewardsService rewardsService;
    
    private UserService userService;
    
    private DtoService dtoService;
    
    @Mock
    private GpsUtil gpsUtil;
    
    @Mock
    private RewardCentral rewardCentral;
    
    @Mock
    private TripPricer tripPricer;
    
    User user;
    VisitedLocation visitedLocation;
    Attraction attraction;
    UserReward userReward;
    UserPreferences userPreferences;
    Location location;
    String username = "Test";
    
    @BeforeEach
    public void setUp() {
        InternalTestHelper.setInternalUserNumber(0);
        rewardsService = new RewardsService(rewardCentral);
        tourGuideService = new TourGuideService(gpsUtil, rewardsService);
        userService = new UserService(rewardsService);
        dtoService = new DtoService(tourGuideService, rewardsService, userService);
        
        
        user = new User(UUID.randomUUID(), "Test", "phoneNumber", "email");
        location = new Location(33.817595, -117.922008);
        visitedLocation = new VisitedLocation(user.getUserId(), location, new java.util.Date());
        attraction = new Attraction("Disneyland", "Anaheim", "CA", 33.817595, -117.922008);
        userReward = new UserReward(visitedLocation, attraction, 158);
        user.addToVisitedLocations(visitedLocation);
        userPreferences = new UserPreferences();
        userService.internalUserMap.put(username, user);
    }
    
    @Test
    void shouldReturnTheNearAttractionListDtoTest() {
        when(gpsUtil.getAttractions()).thenReturn(
                List.of(new Attraction("Disneyland", "Anaheim", "CA", 33.817595, -117.922008)));
        NearAttractionsListDto result = dtoService.nearAttractionsListGenerator(username);
        
        assertEquals(user.getUserId(), result.userId());
        assertEquals(1, result.nearAttractionList()
                .size());
    }
    
    @Test
    void shouldReturnTheUserLocationDtoTest() {
        UserLocationDto result = dtoService.userLocationGenerator(username);
        
        assertEquals(user.getUserId(), result.userId());
        assertEquals(location.latitude, result.location().latitude);
        assertEquals(location.longitude, result.location().longitude);
    }
    
    @Test
    void shouldReturnTheTripDealsDtoTest() {
        TripDealsDto result = dtoService.TripDealListGenerator(username);
        
        assertEquals(user.getUserId(), result.userId());
        assertEquals(5, result.providers().size());
    }
    
    @Test
    void shouldReturnTheUserRewardsDtoTest() {
        UserRewardsDto result = dtoService.UserRewardsListGenerator(username);
        
        assertEquals(user.getUserId(), result.userId());
        assertEquals(user.getUserRewards().size(), result.userRewards().size());
    }
}
