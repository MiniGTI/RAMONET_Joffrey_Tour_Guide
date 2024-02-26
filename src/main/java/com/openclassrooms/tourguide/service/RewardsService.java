package com.openclassrooms.tourguide.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Setter;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

/**
 * Service class to perform rewards treatments
 * <p>
 * Required the GpsUtil external dependency to get the Attraction list.
 * Required the RewardCentral external dependency to perform points attribution.
 * </p>
 *
 * @see GpsUtil
 * @see RewardCentral
 */
@Service
public class RewardsService {
    
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;
    
    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardsCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardsCentral;
    }
    
    /**
     * Convert unit form nautical mile to mile.
     */
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
    
    /**
     * The default proximity in miles.
     */
    private final int defaultProximityBuffer = 10;
    /**
     * The distance in miles to consider the user near to the attraction.
     *
     * @see #nearAttraction(VisitedLocation, Attraction)
     */
    @Setter
    private int proximityBuffer = defaultProximityBuffer;
    /**
     * The distance in miles to consider the user in the proximity range of the attraction.
     *
     * @see #isWithinAttractionProximity(Attraction, Location)
     */
    private final int attractionProximityRange = 200;
    
    
    /**
     * Method to calculate UserRewards.
     * <p>
     * Get the User visitedLocations list and the attraction list of the GpsUtil dependency.
     * Verify in the User.userRewards if the attraction is present.
     * If not verify if the visited location is near the attraction.
     * If true, add a new userReward.
     * </p>
     *
     * @param user the User parsed to calculate rewards.
     * @see GpsUtil#getAttractions()
     */
    public void calculateRewards(User user) {
        List<VisitedLocation> userLocations = user.getVisitedLocations()
                .stream()
                .toList();
        
        List<Attraction> attractions = gpsUtil.getAttractions();
        CopyOnWriteArrayList<UserReward> userRewardsCopy = new CopyOnWriteArrayList<>(user.getUserRewards());
        
        for(VisitedLocation visitedLocation : userLocations) {
            for(Attraction attraction : attractions) {
                if(userRewardsCopy.stream()
                        .noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
                    if(nearAttraction(visitedLocation, attraction)) {
                        userRewardsCopy.add(
                                new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
                    }
                }
            }
        }
        
        user.setUserRewards(userRewardsCopy);
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
     * Return if the distance between the visitedLocation and the attraction is superior to the proximityBuffer value.
     *
     * @param visitedLocation the visitedLocation parsed, to extract the attraction location.
     * @param attraction      the attraction parsed, to extract the attraction location.
     * @return true if superior or false if not.
     * @see #getDistance(Location, Location)
     */
    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return (getDistance(attraction, visitedLocation.location) < proximityBuffer);
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
    public int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }
    
    /**
     * Return the distance between two Locations parsed.
     *
     * @param loc1 first location.
     * @param loc2 second location.
     * @return the distance in miles (double).
     */
    public double getDistance(Location loc1, Location loc2) {
        
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);
        
        double angle =
                Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
        
        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }
}
