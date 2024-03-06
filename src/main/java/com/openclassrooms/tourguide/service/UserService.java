package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class to perform business treatments for Users.
 * <p>
 * Required the RewardService to perform distance and rewards treatments.
 * Required the Tracker to generate the location of users.
 * </p>
 * <p>
 * Use ExecutorService to perform multiThreads treatments.
 * Call the Tracker to calculate the users location.
 * </p>
 *
 * @see RewardsService
 * @see Tracker
 */
@Service
@Slf4j
public class UserService {
    
    private final RewardsService rewardsService;
    public final Tracker tracker;
    public final Map<String, User> internalUserMap = new HashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(1000);
    
    public UserService(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
        
        tracker = new Tracker(this);
        
        addShutDownHook();
    }
    
    public List<UserReward> getUserRewards(User user) {
        rewardsService.calculateUserRewards(user);
        return user.getUserRewards();
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
    protected VisitedLocation getUserLocation(String userName) {
        User user = getUserByUsername(userName);
        
        return user.getVisitedLocations()
                .isEmpty() ? trackUserLocation(user) : user.getLastVisitedLocation();
    }
    
    /**
     * Method to call the TrackUserCallable in a new Thread.
     * <p>
     * Use the FixedThreadPool of the executorService to call TrackUserCallable(user).
     * </p>
     *
     * @param user User parsed to calculate the current Location.
     * @return the visitedLocation.
     * @see TrackUserCallable
     */
    public VisitedLocation trackUserLocation(User user) {
        
        Callable<VisitedLocation> callable = new TrackUserCallable(user);
        Future<VisitedLocation> future = executorService.submit(callable);
        
        try {
            VisitedLocation visitedLocation = future.get();
            executorService.shutdown();
            return visitedLocation;
        } catch(InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Method to Track all User location.
     * <p>
     * Create a List<Future<VisitedLocation> to prepare Threads.
     * Use the FixedThreadPool of the executorService to call TrackUserCallable(user) for each Users.
     * </p>
     *
     * @see #trackUserLocation(User)
     */
    public void trackAllUser() {
        List<User> users = getAllUsers();
        
        List<Future<VisitedLocation>> futures = new ArrayList<>();
        
        for(User user : users) {
            Future<VisitedLocation> future = executorService.submit(new TrackUserCallable(user));
            futures.add(future);
        }
        
        List<VisitedLocation> visitedLocations = new ArrayList<>();
        
        for(Future<VisitedLocation> future : futures) {
            try {
                visitedLocations.add(future.get());
                executorService.shutdown();
            } catch(InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        log.debug("********** TrackAllUser calculate: " + visitedLocations.size() + " VisitedLocation. **********");
    }
    
    
    private void addShutDownHook() {
        Runtime.getRuntime()
                .addShutdownHook(new Thread() {
                    public void run() {
                        tracker.stopTracking();
                    }
                });
    }
    
    /**
     * The Callable class to create Threads in the UserService class to get the User location.
     * <p>
     * Required the GpsUtil external dependency to get the Attraction list.
     * </p>
     *
     * @see UserService#trackUserLocation(User)
     */
    private static class TrackUserCallable implements Callable<VisitedLocation>{
        private final User user;
        private final GpsUtil gpsUtil = new GpsUtil();
        
        private TrackUserCallable(User user) {
            this.user = user;
        }
        
        /**
         * The override call method of Callable implementation.
         * <p>
         * Call getUserLocation method of the GpsUtil lib to generate a random location.
         * Call the addToVisitedLocations method of User to add this location into the user's VisitedLocation.
         * </p>
         *
         * @return the VisitedLocation generated.
         * @see GpsUtil#getUserLocation(UUID)
         * @see User#addToVisitedLocations(VisitedLocation)
         */
        @Override
        public VisitedLocation call() {
            log.debug("TrackUserRunnable n° " + Thread.currentThread()
                    .getName() + " started.");
            log.debug("Calculates " + user.getUserId() + "'s location.");
            log.debug("VisitedLocations size before update: " + user.getVisitedLocations()
                    .size());
            VisitedLocation userLocation = gpsUtil.getUserLocation(user.getUserId());
            user.addToVisitedLocations(userLocation);
            
            log.debug("VisitedLocations size after update: " + user.getVisitedLocations()
                    .size());
            log.debug("TrackUserRunnable ended.");
            
            return userLocation;
        }
    }
}
