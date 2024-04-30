package com.openclassrooms.tourguide.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import gpsUtil.location.VisitedLocation;
import lombok.Getter;
import lombok.Setter;
import tripPricer.Provider;

/**
 * The model to represent user and store locations, rewards, preferences, and personal's information.
 */
@Setter
@Getter
public class User {
    private final UUID userId;
    private final String userName;
    private String phoneNumber;
    private String emailAddress;
    private Date latestLocationTimestamp;
    private List<VisitedLocation> visitedLocations = new ArrayList<>();
    private List<UserReward> userRewards = new ArrayList<>();
    private UserPreferences userPreferences = new UserPreferences();
    private List<Provider> tripDeals = new ArrayList<>();
    
    public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }
    
    public void addUserReward(List<UserReward> newUserRewards) {
        for(UserReward userReward : newUserRewards) {
            addUserReward(userReward);
        }
    }
    
    /**
     * Method to verify if the attraction of the UserReward parsed is already rewarded in the user's userRewards.
     * <p>
     * @param userReward the userReward to save into the user's userRewards.
     * </p>
     */
    public void addUserReward(UserReward userReward) {
        if(userRewards.stream()
                .noneMatch(u -> u.attraction.attractionName.contains(userReward.attraction.attractionName))) {
            userRewards.add(userReward);
        }
    }
    
    public VisitedLocation getLastVisitedLocation() {
        return visitedLocations.getLast();
    }
    
    public void addToVisitedLocations(VisitedLocation visitedLocation) {
        visitedLocations.add(visitedLocation);
    }
    
    public void clearVisitedLocations() {
        visitedLocations.clear();
    }
}
