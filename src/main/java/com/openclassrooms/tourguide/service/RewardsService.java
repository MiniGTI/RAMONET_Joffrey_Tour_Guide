package com.openclassrooms.tourguide.service;

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

import gpsUtil.location.VisitedLocation;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import tripPricer.TripPricer;

/**
 * Service class to perform rewards treatments
 * <p>
 * Required the GpsUtil external dependency to get the Attraction list.
 * Required the RewardCentral external dependency to perform points attribution.
 * </p>
 * <p>
 * Use ExecutorService to perform multiThreads treatments.
 * </p>
 *
 * @see GpsUtil
 * @see RewardCentral
 */
@Service
@Slf4j
public class RewardsService {
    private final RewardCentral rewardsCentral;
    
    public RewardsService(RewardCentral rewardsCentral) {
        this.rewardsCentral = rewardsCentral;
    }
    
    /**
     * Convert unit form nautical mile to mile.
     */
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
    
    /**
     * The default proximity in miles.
     */
    private int defaultProximityBuffer = 10;
    /**
     * The distance in miles to consider the user near to the attraction.
     */
    @Setter
    @Getter
    private int proximityBuffer = defaultProximityBuffer;
    
    
    /**
     * The distance in miles to consider the user in the proximity range of the attraction.
     *
     * @see #isWithinAttractionProximity(Attraction, Location)
     */
    private final int attractionProximityRange = 200;
    
    
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    
    /**
     * Method to calculate the UserRewards of a List<User>.
     * <p>
     * The calculation is slow because of the RewardCentral response.
     * To overcome this slowdown the executorService managed a ThreadPool and generate virtualThreads for each iterate.
     * </p>
     *
     * @param users the List<User>
     * @see #calculateUserRewards(User)
     * @see RewardCentral
     */
    public void calculateAllUsersRewards(List<User> users) {
        List<Future<List<UserReward>>> futures = new ArrayList<>();
        for(User user : users) {
            Future<List<UserReward>> future = executorService.submit(new CalculateUserRewardsCallable(user, this));
            futures.add(future);
        }
        
        List<List<UserReward>> userRewardsLists = new ArrayList<>();
        for(Future<List<UserReward>> future : futures) {
            try {
                userRewardsLists.add(future.get());
                executorService.shutdown();
            } catch(InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        
        log.debug("********** calculateAllUsersRewards calculate: " + userRewardsLists.size() +
                " List of UserRewards. **********");
    }
    
    
    /**
     * Return if the distance between the attraction and the location parsed is superior to the attractionProximityRange.
     *
     * @param attraction the attraction parsed, to extract the attraction location.
     * @param location   the location parsed.
     * @return false if superior and true if not.
     * @see #getDistance(Location, Location)
     */
    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return (getDistance(attraction, location) < attractionProximityRange);
    }
    
    
    /**
     * Method to calculate the UserRewards of an User.
     *
     * <p>
     * The calculation is slow because of the RewardCentral response.
     * To overcome this slowdown the executorService managed a ThreadPool and generate virtualThreads for each iterate.
     * </p>
     *
     * @param user the User parsed.
     * @return a List of UserReward.
     * @see #calculateUserRewards(User)
     * @see RewardCentral
     */
    public List<UserReward> calculateUserRewards(User user) {
        
        Callable<List<UserReward>> callable = new CalculateUserRewardsCallable(user, this);
        Future<List<UserReward>> future = executorService.submit(callable);
        
        try {
            return future.get();
        } catch(InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Return the distance between two Locations parsed.
     *
     * @param loc1 first location.
     * @param loc2 second location.
     * @return the distance in miles (double).
     */
    protected double getDistance(Location loc1, Location loc2) {
        
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);
        
        double angle =
                Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
        
        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }
    
    /**
     * Call the rewardsCentral dependency to give a random rewardPoint from 1 to 1000.
     * <p>
     * For the moment parameters is not used in the method.
     *
     * @param attraction the attraction visited.
     * @param user       the user connected.
     * @return the rewardPoint (int).
     * @see RewardCentral#getAttractionRewardPoints(UUID, UUID)
     */
    protected int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }
    
    
    /**
     * The Callable class to create Threads in the RewardService class to calculate the userRewards.
     * <p>
     * Required the GpsUtil external dependency to get the Attraction list.
     * Required the RewardService to perform distance and rewards treatments.
     * </p>
     *
     * @see GpsUtil
     * @see RewardsService
     * @see TripPricer
     */
    @Slf4j
    private static class CalculateUserRewardsCallable implements Callable<List<UserReward>> {
        
        private final User user;
        private final GpsUtil gpsUtil = new GpsUtil();
        private final RewardsService rewardsService;
        
        private CalculateUserRewardsCallable(User user, RewardsService rewardsService) {
            this.user = user;
            this.rewardsService = rewardsService;
        }
        
        /**
         * Call method override from Callable, calculate the actual UserRewards of a User.
         * <p>
         * Call the userRewardListFilter method with the user's UserRewards to get a List<Attraction> without Attractions rewarded.
         * Call the nearAttraction method with all combinations of user's VisitedLocation and Attraction of the list returned by the previous step.
         * If nearAttraction is true, add the combination in to a Map<Attraction, VisitedLocation>.
         * Finally parse the map to the mapToSetUserRewards to add the new UserRewards to the User.
         * </p>
         *
         * @see User
         * @see #nearAttraction(VisitedLocation, Attraction)
         * @see #mapToSetUserRewards(Map, User)
         */
        @Override
        public List<UserReward> call() {
            
            log.debug("CalculateUserRewardsCallable n° " + Thread.currentThread()
                    .getName() + " started.");
            log.debug("Calculates " + user.getUserId() + "'s UserRewards.");
            log.debug("UserRewards size before update: " + user.getUserRewards()
                    .size());
            
            List<Attraction> attractions = userRewardListFilter(user.getUserRewards());
            Map<Attraction, VisitedLocation> newUserRewardsMap = new HashMap<>();
            
            user.getVisitedLocations()
                    .parallelStream()
                    .forEach(visitedLocation -> {
                        attractions.parallelStream()
                                .forEach(attraction -> {
                                    if(nearAttraction(visitedLocation, attraction)) {
                                        newUserRewardsMap.put(attraction, visitedLocation);
                                    }
                                });
                    });
            
            List<UserReward> userRewards = mapToSetUserRewards(newUserRewardsMap, user);
            log.debug("UserRewards size after update: " + user.getUserRewards()
                    .size());
            log.debug("CalculateUserRewardsCallable ended.");
            return userRewards;
        }
        
        /**
         * Method to get all Attractions that are not present in the user's UserRewards.
         * <p>
         * Call getAttractions method of the GpsUtil lib to create a List<Attraction> of all attractions.
         * Map the List<UserReward> parsed in to a List<String> of name of all UserReward's Attractions.
         * </p>
         *
         * @param userRewards the user's UserReward list.
         * @return a List<Attraction> of all attraction that are not present in the initial List<UserReward>.
         */
        private List<Attraction> userRewardListFilter(List<UserReward> userRewards) {
            List<Attraction> attractions = gpsUtil.getAttractions();
            List<String> userRewardsAttractionList = userRewards.stream()
                    .map(userReward -> userReward.getAttraction().attractionName)
                    .toList();
            return attractions.parallelStream()
                    .filter(attraction -> !userRewardsAttractionList.contains(attraction.attractionName))
                    .toList();
        }
        
        /**
         * Method to verify if the distance between VisitedLocation and the attraction is less the proximityBuffer value.
         *
         * @param visitedLocation the user's location.
         * @param attraction      to get the attraction location.
         * @return true if the distance is less and false if more.
         * @see RewardsService#getDistance(Location, Location)
         */
        private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
            return rewardsService.getDistance(attraction, visitedLocation.location) <
                    rewardsService.getProximityBuffer();
        }
        
        /**
         * Method to add new UserRewards in to the user's userRewards.
         * <p>
         * Get a Map<Attraction, VisitedLocation> and a User.
         * For each pair key/value, create a new UserReward and add it in to a List<UserRewards>.
         * Finally call the addUserReward method of the User parsed to save the list.
         * </p>
         * <p>
         * The calculation is slow because of the call of the getRewardPoints method who call the rewardCentral API.
         * To overcome this slowdown the executorService managed a ThreadPool and generate virtualThreads for each iterate.
         * </p>
         *
         * @param newUserRewardsMap the map with all data to create new UserRewards.
         * @param user              the user required UserReward update.
         *                          //  * @see User#addUserReward(List)
         * @see RewardsService#getRewardPoints(Attraction, User)
         */
        private List<UserReward> mapToSetUserRewards(Map<Attraction, VisitedLocation> newUserRewardsMap, User user) {
            ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
            List<UserReward> userRewardsToSave = new ArrayList<>();
            
            List<Future<UserReward>> futures = new ArrayList<>();
            for(Map.Entry<Attraction, VisitedLocation> entry : newUserRewardsMap.entrySet()) {
                Future<UserReward> future = executorService.submit(
                        () -> new UserReward(entry.getValue(), entry.getKey(),
                                rewardsService.getRewardPoints(entry.getKey(), user)));
                futures.add(future);
            }
            
            for(Future<UserReward> future : futures) {
                try {
                    
                    userRewardsToSave.add(future.get());
                } catch(InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            
            user.addUserReward(userRewardsToSave);
            
            executorService.shutdownNow();
            return userRewardsToSave;
        }
    }
}
