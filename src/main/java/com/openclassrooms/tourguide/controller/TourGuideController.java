package com.openclassrooms.tourguide.controller;

import com.openclassrooms.tourguide.dto.NearAttractionsListDto;
import com.openclassrooms.tourguide.dto.TripDealsDto;
import com.openclassrooms.tourguide.dto.UserLocationDto;
import com.openclassrooms.tourguide.dto.UserRewardsDto;
import com.openclassrooms.tourguide.service.DtoService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * The Api controller.
 * <p>
 * Call DtoService to get responses of all endpoints.
 *
 * @see DtoService
 */
@AllArgsConstructor
@RestController
public class TourGuideController {
    
    /**
     * DtoService is the only service called by the controller.
     *
     * @see DtoService
     */
    private final DtoService dtoService;
    
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
     * Call the userLocationGenerator to parse the current location of the user.
     * </p>
     *
     * @param userName the parameter parsed to get the current user.
     * @return the UserLocationDto object.
     * @see DtoService#userLocationGenerator(String)
     */
    @RequestMapping("/getLocation")
    public UserLocationDto getLocation(
            @RequestParam String userName) {
        return dtoService.userLocationGenerator(userName);
    }
    
    /**
     * Method to manage the /getNearbyByAttractions?userName endPoint.
     * <p>
     * Call the nearAttractionsListGenerator method to get the NearAttractionsListDto who contains the fives closest attractions.
     * </p>
     *
     * @param userName the parameter parsed to get the current user.
     * @return the NearAttractionsListDto object.
     * @see DtoService#nearAttractionsListGenerator(String)
     */
    @RequestMapping("/getNearbyAttractions")
    public NearAttractionsListDto getNearbyAttractions(
            @RequestParam String userName) {
        return dtoService.nearAttractionsListGenerator(userName);
    }
    
    /**
     * Method to manage the /getRewards?userName endPoint.
     * <p>
     * Call the UserRewardsListGenerator method to get the UserRewardsDto who contains the userId and all UserRewards.
     * </p>
     *
     * @param userName the parameter parsed to get the current user.
     * @return the UserRewardsDto object.
     * @see DtoService#UserRewardsListGenerator(String)
     */
    @RequestMapping("/getRewards")
    public UserRewardsDto getRewards(
            @RequestParam String userName) {
        return dtoService.UserRewardsListGenerator(userName);
    }
    
    /**
     * Method to manage the /TripDeals?userName endPoint.
     * <p>
     * Call the TripDealListGenerator method to get the fives latest Trip and the userId of the user parsed.
     * </p>
     *
     * @param userName the user parsed to get latest trips.
     * @return the TripDealsDto object.
     * @see DtoService#TripDealListGenerator(String)
     */
    @RequestMapping("/getTripDeals")
    public TripDealsDto getTripDeals(
            @RequestParam String userName) {
        return dtoService.TripDealListGenerator(userName);
    }
}