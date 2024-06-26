package com.openclassrooms.tourguide.user;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Model to represent a reward.
 */
@Setter
@Getter
@AllArgsConstructor
public class UserReward {
    
    public final VisitedLocation visitedLocation;
    public final Attraction attraction;
    private int rewardPoints;
    
    public UserReward(VisitedLocation visitedLocation, Attraction attraction) {
        this.visitedLocation = visitedLocation;
        this.attraction = attraction;
    }
}
