package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.dto.NearAttractionDto;
import com.openclassrooms.tourguide.dto.NearAttractionsListDto;
import com.openclassrooms.tourguide.dto.TripDealsDto;
import com.openclassrooms.tourguide.dto.UserLocationDto;
import com.openclassrooms.tourguide.dto.UserRewardsDto;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import tripPricer.Provider;

import java.util.ArrayList;
import java.util.List;

/**
 * DtoService is called by the TourGuideController to generate JSON.
 * <p>
 * Call TourGuideService and RewardsService to perform business treatments.
 * </p>
 * @see TourGuideService
 * @see RewardsService
 */
@Service
public class DtoService {
    
    private final TourGuideService tourGuideService;
    private final RewardsService rewardsService;
    
    public DtoService(TourGuideService tourGuideService, RewardsService rewardsService) {
        this.tourGuideService = tourGuideService;
        this.rewardsService = rewardsService;
    }
    
    /**
     * Return the NearAttractionListDto to display the JSON into the getNearbyAttractions in the TourGuideController.
     * <p>
     * For each Attraction in the list parsed, creates a new NearAttractionDto and adds it in the NearAttractionsListDto.
     * </p>
     *
     * @param userName the userName parsed to get the User and called the getNearByAttractions method.
     * @return a NearAttractionsListDto object.
     * @see TourGuideService#getUserByUsername(String)
     * @see TourGuideService#getNearByAttractions(VisitedLocation)
     * @see RewardsService#getDistance(Location, Location)
     * @see RewardsService#getRewardPoints(Attraction, User)
     */
    public NearAttractionsListDto nearAttractionsListGenerator(String userName) {
        List<NearAttractionDto> nearAttractionsList = new ArrayList<>();
        
        User user = tourGuideService.getUserByUsername(userName);
        List<Attraction> attractions = tourGuideService.getNearByAttractions(user.getLastVisitedLocation());
        
        for(Attraction attraction : attractions) {
            nearAttractionsList.add(NearAttractionDto.builder()
                    .name(attraction.attractionName)
                    .longitude(attraction.longitude)
                    .latitude(attraction.latitude)
                    .userLongitude(user.getLastVisitedLocation().location.longitude)
                    .userLatitude(user.getLastVisitedLocation().location.latitude)
                    .distance(rewardsService.getDistance(attraction, user.getLastVisitedLocation().location))
                    .rewardPoints(rewardsService.getRewardPoints(attraction, user))
                    .build());
        }
        
        return NearAttractionsListDto.builder()
                .userId(user.getUserId())
                .nearAttractionList(nearAttractionsList)
                .build();
    }
    
    /**
     * Return the UserLocationDto to display the JSON into the getLocation in the TourGuideController.
     * <p>
     * Parse the VisitedLocation returned by the tourGuideService.getUserLocation method into a UserLocationDto.
     * </p>
     *
     * @param userName the userName parsed to call the getUserLocation method.
     * @return a UserLocationDto object.
     * @see TourGuideService#getUserLocation(String)
     */
    public UserLocationDto userLocationGenerator(String userName) {
        VisitedLocation userLocation = tourGuideService.getUserLocation(userName);
        return UserLocationDto.builder()
                .userID(userLocation.userId)
                .location(userLocation.location)
                .timeVisited(userLocation.timeVisited)
                .build();
    }
    
    /**
     * Return the TripDealsDto to display the JSON into the getTripDeals in the TourGuideController.
     * <p>
     * Parse the List of providers returned by the tourGuideService.getTripDeals method into a TripDealsDto.
     * </p>
     *
     * @param userName the userName parsed to get the User and called the getUserLocation method and getUserByUsername methods.
     * @return a TripDealsDto object.
     * @see TourGuideService#getUserByUsername(String)
     * @see TourGuideService#getTripDeals(User)
     */
    public TripDealsDto TripDealListGenerator(String userName) {
        User user = tourGuideService.getUserByUsername(userName);
        List<Provider> tripDeals = tourGuideService.getTripDeals(user);
        
        return TripDealsDto.builder()
                .userId(user.getUserId())
                .providers(tripDeals)
                .build();
    }
    
    /**
     * Return the UserRewardsDto to display the JSON into the getRewards in the TourGuideController.
     * <p>
     * Parse the List of UserReward returned by the tourGuideService.getUserRewards method, and add the userId into a UserRewardsDto.
     * </p>
     *
     * @param userName the userName parsed to get the User and called getUserRewards and getUserByUsername methods.
     * @return a UserRewardsDto object.
     * @see TourGuideService#getUserByUsername(String)
     * @see TourGuideService#getUserRewards(User)
     */
    public UserRewardsDto UserRewardsListGenerator(String userName) {
        User user = tourGuideService.getUserByUsername(userName);
        List<UserReward> userRewards = tourGuideService.getUserRewards(user);
        
        return UserRewardsDto.builder()
                .userId(user.getUserId())
                .userRewards(userRewards)
                .build();
    }
}
