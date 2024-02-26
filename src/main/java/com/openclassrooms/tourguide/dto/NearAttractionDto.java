package com.openclassrooms.tourguide.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Dto model to constitute the NearAttractionListDto for the getNearbyAttractions method into the TourGuideController.
 * <p>
 * Stoked into a List in a NearAttractionListDto object.
 * </p>
 *
 * @see NearAttractionsListDto
 * @see com.openclassrooms.tourguide.service.DtoService#nearAttractionsListGenerator(String)
 */
@Getter
@Setter
@Builder
public class NearAttractionDto {
    
    private String name;
    private Double longitude;
    private Double latitude;
    private Double userLongitude;
    private Double userLatitude;
    private Double distance;
    private int rewardPoints;
}
