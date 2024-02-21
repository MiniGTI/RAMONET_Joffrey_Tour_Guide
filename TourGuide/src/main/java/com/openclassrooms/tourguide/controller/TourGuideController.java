package com.openclassrooms.tourguide.controller;

import java.util.List;

import com.openclassrooms.tourguide.dto.NearAttractionsListDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.UserReward;

import tripPricer.Provider;

/**
 * The Api controller.
 */
@AllArgsConstructor
@RestController
public class TourGuideController {
    
    
    private final TourGuideService tourGuideService;
    
    /**
     * Method to manage the home page.
     *
     * @return the home page.
     */
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    /**
     * Method to manage the /getLocation?userName endPoint.
     * <p>
     * Call the getUserLocation parse the current location of the user.
     * </p>
     *
     * @param userName the parameter parsed to get the current user.
     * @return the VisitedLocation object to parse the JSON.
     * @see TourGuideService#getUserLocation(String userName)
     */
    @RequestMapping("/getLocation")
    public VisitedLocation getLocation(
            @RequestParam String userName) {
        return tourGuideService.getUserLocation(userName);
    }
    
    /**
     * Method to manage the /getNearbyByAttractions?userName endPoint.
     * <p>
     * Call the nearAttractionsListGenerator method to get the NearAttractionsListDto who contains the fives closest attractions.
     * </p>
     *
     * @param userName the parameter parsed to get the current user.
     * @return the NearAttractionsListDto object to parse the JSON.
     * @see TourGuideService#nearAttractionsListGenerator(String)
     */
    @RequestMapping("/getNearbyAttractions")
    public NearAttractionsListDto getNearbyAttractions(
            @RequestParam String userName) {
        return tourGuideService.nearAttractionsListGenerator(userName);
    }
    
    @RequestMapping("/getRewards")
    public List<UserReward> getRewards(
            @RequestParam String userName) {
        return tourGuideService.getUserRewards(userName);
    }
    
    /**
     * Method to manage the /TripDeals?userName endPoint.
     * <p>
     * Call the getTripDeals method to get the fives latest Trip of the user parsed.
     * </p>
     *
     * @param userName the user parsed to get latest trips.
     * @return the list of provider object to parse the JSON.
     */
    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(
            @RequestParam String userName) {
        return tourGuideService.getTripDeals(userName);
    }
}