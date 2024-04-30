package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;
import tripPricer.TripPricer;

/**
 * Service class to perform user treatments.
 * <p>
 * Required the GpsUtil external dependency to get the Attraction list.
 * Required the RewardService to perform distance and rewards treatments.
 * Required the TripPricer external dependency to get the provider price.
 * </p>
 *
 * @see GpsUtil
 * @see RewardsService
 * @see TripPricer
 */
@Service
@Slf4j
public class TourGuideService {
    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    
    /**
     * *********************************
     * Required to be updated for a production use.
     * *********************************
     */
    private static final String tripPricerApiKey = "test-server-api-key";
    
    
    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;
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
     *
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
}