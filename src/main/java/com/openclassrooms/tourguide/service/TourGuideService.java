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

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tripPricer.Provider;
import tripPricer.TripPricer;

/**
 * Service class to perform user treatments
 * <p>
 * Required the GpsUtil external dependency to get the Attraction list.
 * Required the RewardService to perform distance and rewards treatments.
 * Required the TripPricer external dependency to get the provider price.
 * Required the Tracker to generate the location of users.
 * </p>
 * <p>
 * *********************************
 * Not production
 * *********************************
 * Contains all methods to perform an in memory database.
 * Call InternalTestHelper to know how many user we need to create.
 * And generate users to test the application performances.
 * </p>
 * <p>
 * *********************************
 * For production use
 * *********************************
 * For a production use, we need to turn testMode false to disable the in memory users generation.
 * We need also to set up an external database.
 * </p>
 *
 * @see GpsUtil
 * @see RewardsService
 * @see TripPricer
 * @see Tracker
 */
@Service
@Slf4j
public class TourGuideService {
    
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
            log.info("TestMode enabled");
            log.debug("Initializing users");
            initializeInternalUsers();
            log.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }
    
    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }
    
    /**
     * To get the actual User Location.
     * <p>
     * If the User.visitedLocation is empty, return a random position.
     * if not, call getLastVisitedLocation method
     * </p>
     *
     * @param userName the userName parsed to get the User location.
     * @return a VisitedLocation.
     * @see User#getLastVisitedLocation()
     */
    public VisitedLocation getUserLocation(String userName) {
        User user = getUserByUsername(userName);
        return user.getVisitedLocations()
                .isEmpty() ? trackUserLocation(user) : user.getLastVisitedLocation();
    }
    
    public User getUserByUsername(String userName) {
        return internalUserMap.get(userName);
    }
    
    public List<User> getAllUsers() {
        return new ArrayList<>(internalUserMap.values());
    }
    
    public void addUser(User user) {
        if(!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }
    
    /**
     * Generated a random location for the User parsed.
     * <p>
     * Next call the calculateRewards to verify if the user is in an attraction radius.
     * </p>
     * @param user User parsed to calculate the current Location.
     * @return the visitedLocation.
     * @see GpsUtil#getUserLocation(UUID)
     * @see RewardsService#calculateRewards(User)
     */
    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }
    
    
    /**
     * Return the list of providers for a user's TripDeal.
     * <p>
     * Get the sum of the User.userRewards.rewardPoints.
     * Call getPrice method of tripPricer to create a list of Provider with params:
     * apiKey : an arbitrary String.
     * user : the in memory user.
     * The UserPreference is numberOfAdults = 1, numberOfChildren = 0, tripDuration = 1.
     * </p>
     * @param user the user parsed to create the trip deals list.
     * @return the list of provider who set to the user.
     * @see TripPricer#getPrice(String, UUID, int, int, int, int)
     */
    public List<Provider> getTripDeals(User user) {
        int cumulatativeRewardPoints = user.getUserRewards()
                .stream()
                .mapToInt(UserReward::getRewardPoints)
                .sum();
        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences()
                .getNumberOfAdults(), user.getUserPreferences()
                .getNumberOfChildren(), user.getUserPreferences()
                .getTripDuration(), cumulatativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }
    
    
    /**
     * Return a list of the five attractions closest to the visitedLocation parsed.
     * <p>
     * Get the list of all attractions.
     * Generate a stream of Map<Attraction, Double(distance between the visitedLocation and the Attraction).
     * Sorted the Map by values.
     * And return a list with the first fives entries.
     * </p>
     *
     * @param visitedLocation the location parsed.
     * @return a list of five Attraction .
     * @see GpsUtil#getAttractions()
     */
    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
        return gpsUtil.getAttractions()
                .stream()
                .collect(Collectors.toMap(attraction -> attraction,
                        distance -> rewardsService.getDistance(visitedLocation.location, distance)))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .limit(5)
                .toList();
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
        log.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
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