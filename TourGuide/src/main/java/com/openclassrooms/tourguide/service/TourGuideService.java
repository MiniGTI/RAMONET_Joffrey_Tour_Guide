package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;

import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
    private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;
    boolean testMode = true;
    
    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;
        
        Locale.setDefault(Locale.US);
        
        if(testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }
    
    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }
    
    /**
     * To get the actual User Location.
     * If the User.visitedLocation is not empty, return the last visitedLocation.
     * if not, return a random position.
     *
     * @param user the user parses to get location.
     * @return a VisitedLocation.
     */
    public VisitedLocation getUserLocation(User user) {
        VisitedLocation visitedLocation = (user.getVisitedLocations()
                .size() > 0) ? user.getLastVisitedLocation() : trackUserLocation(user);
        return visitedLocation;
    }
    
    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }
    
    public List<User> getAllUsers() {
        return internalUserMap.values()
                .stream()
                .collect(Collectors.toList());
    }
    
    public void addUser(User user) {
        if(!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }
    
    /**
     * Get the sum of the User.userRewards.rewardPoints.
     * Call getPrice method of tripPricer to create a list of Provider with params:
     * apiKey : an arbitrary String.
     * user : the in memory user.
     * The UserPreference is numberOfAdults = 1, numberOfChildren = 0, tripDuration = 1.
     * @see TripPricer#getPrice(String, UUID, int, int, int, int)
     * @param user the user parsed to set his tripDeal.
     * @return the list of provider who set to the user.
     */
    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards()
                .stream()
                .mapToInt(i -> i.getRewardPoints())
                .sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences()
                .getNumberOfAdults(), user.getUserPreferences()
                .getNumberOfChildren(), user.getUserPreferences()
                .getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }
    
    /**
     * Generated a random location for the User parsed.
     * Next call the calculateRewards to verify if the user is an attraction radius.
     *
     * @see GpsUtil#getUserLocation(UUID)
     * @see RewardsService#calculateRewards(User)
     * @param user User parsed to calculate the current Location.
     * @return the visitedLocation.
     */
    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }
    
    /**
     * Get all attractions and verify if the visitedLocation parsed is in the attraction range proximity.
     * @see GpsUtil#getAttractions()
     * @see RewardsService#isWithinAttractionProximity(Attraction, Location)
     * @param visitedLocation the location parsed.
     * @return a list of all attraction where in the range.
     */
    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
        List<Attraction> nearbyAttractions = new ArrayList<>();
        for(Attraction attraction : gpsUtil.getAttractions()) {
            if(rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
                nearbyAttractions.add(attraction);
            }
        }
        return nearbyAttractions;
    }
    
    private void addShutDownHook() {
        Runtime.getRuntime()
                .addShutdownHook(new Thread() {
                    public void run() {
                        tracker.stopTracking();
                    }
                });
    }
    
    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String tripPricerApiKey = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes
    // internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();
    
    /**
     * Method to generate an in memory user pool for tests.
     * By default, generate a pool with 101 users.
     */
    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber())
                .forEach(i -> {
                    String userName = "internalUser" + i;
                    String phone = "000";
                    String email = userName + "@tourGuide.com";
                    User user = new User(UUID.randomUUID(), userName, phone, email);
                    generateUserLocationHistory(user);
                    
                    internalUserMap.put(userName, user);
                });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }
    
    /**
     * Method to generate a history location for all in memory user test.
     * Generate 4 locations with a random Location and a random Time.
     *
     * @param user mocked user parsed.
     */
    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3)
                .forEach(i -> {
                    user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
                            new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
                });
    }
    
    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }
    
    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }
    
    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now()
                .minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }
}
