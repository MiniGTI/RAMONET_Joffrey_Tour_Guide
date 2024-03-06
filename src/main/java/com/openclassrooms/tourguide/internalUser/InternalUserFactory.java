package com.openclassrooms.tourguide.internalUser;

import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.user.User;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 *  Bean factory to generate the in memory pool of Users.
 *  <p>
 *      Required UserService to add new users.
 *  </p>
 *  <p>
 *  *********************************
 *  Not production
 *  *********************************
 *  Contains all methods to perform an in memory database.
 *  Call InternalTestHelper to know how many user we need to create.
 *  And generate users to test the application performances.
 *  </p>
 *  <p>
 *  *********************************
 *  For production use
 *  *********************************
 *  For a production use, we need to turn testMode false to disable the in memory users generation.
 *  We need also to set up an external database.
 *  </p>
 *  @see UserService
 */
@Slf4j
@Service
public class InternalUserFactory {
    
    private final UserService userService;
    
    boolean testMode = true;
    
    public InternalUserFactory(UserService userService) {
        this.userService = userService;
        
        Locale.setDefault(Locale.US);
        
        if(testMode) {
            log.info("TestMode enabled");
            log.debug("Initializing users");
            initializeInternalUsers();
            log.debug("Finished initializing users");
        }
    }
    
    
    /**
     * Method to generate an in memory user pool for tests.
     * By default, generate a pool with 101 users.
     */
    public void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber())
                .forEach(i -> {
                    String userName = "internalUser" + i;
                    String phone = "000";
                    String email = userName + "@tourGuide.com";
                    User user = new User(UUID.randomUUID(), userName, phone, email);
                    generateUserLocationHistory(user);
                    
                    userService.internalUserMap.put(userName, user);
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
