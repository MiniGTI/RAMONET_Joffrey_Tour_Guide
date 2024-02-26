package com.openclassrooms.tourguide.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Dto model to meet the getNearbyAttractions method into the TourGuideController.
 * <p>
 * Returned by nearAttractionsListGenerator in the DtoService.
 * </p>
 *
 * @see NearAttractionDto
 * @see com.openclassrooms.tourguide.service.DtoService#nearAttractionsListGenerator(String)
 * @see com.openclassrooms.tourguide.controller.TourGuideController#getNearbyAttractions(String)
 */
@Setter
@Getter
@Builder
public class NearAttractionsListDto {
    
    private UUID userId;
    private List<NearAttractionDto> nearAttractionList;
}
