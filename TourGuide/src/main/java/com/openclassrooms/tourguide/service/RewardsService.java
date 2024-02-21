package com.openclassrooms.tourguide.service;

import java.util.List;

import lombok.Setter;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
    
    // proximity in miles
    
    private final int defaultProximityBuffer = 10;
    @Setter
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;
    
    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }
    
    /**
     * Get the User visitedLocations list and the attraction list.
     * Verify in the User.userRewards if the attraction is present.
     * If not verify is the visited location is near the attraction.
     * If true, add a new userReward.
     *
     * @param user the User parsed to calculate rewards.
     */
    public void calculateRewards(User user) {
        List<VisitedLocation> userLocations = user.getVisitedLocations();
        List<Attraction> attractions = gpsUtil.getAttractions();
        
        for(VisitedLocation visitedLocation : userLocations) {
            for(Attraction attraction : attractions) {
                if(user.getUserRewards()
                        .stream()
                        .noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
                    if(nearAttraction(visitedLocation, attraction)) {
                        user.addUserReward(
                                new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
                    }
                }
            }
        }
    }
    
    /**
     * Return if the distance between the attraction and the location parsed is superior to the attractionProximityRange.
     *
     * @param attraction the attraction parsed, to extract the attraction location.
     * @param location   the location parsed.
     * @return false if superior and true if not.
     */
    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return !(getDistance(attraction, location) > attractionProximityRange);
    }
    
    /**
     * Return if the distance between the visitedLocation and the attraction is superior to the proximityBuffer value.
     *
     * @param visitedLocation the visitedLocation parsed, to extract the attraction location.
     * @param attraction      the attraction parsed, to extract the attraction location.
     * @return true if superior or false if not.
     */
    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
    }
    
    /**
     * Give a random rewardPoint from 1 to 1000.
     *
     * @param attraction the attraction visited.
     * @param user       the user connected.
     * @return the rewardPoint (int).
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
