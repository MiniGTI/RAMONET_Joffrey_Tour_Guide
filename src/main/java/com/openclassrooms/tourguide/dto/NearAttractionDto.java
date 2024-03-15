package com.openclassrooms.tourguide.dto;

import lombok.Builder;

/**
 * Dto model to constitute the NearAttractionListDto for the getNearbyAttractions method into the TourGuideController.
 * <p>
 * Stoked into a List in a NearAttractionListDto object.
 * </p>
 *
 * @see NearAttractionsListDto
 * @see com.openclassrooms.tourguide.service.DtoService#nearAttractionsListGenerator(String)
 */

@Builder
public record NearAttractionDto(String name, Double longitude, Double latitude, Double userLongitude, Double userLatitude, Double distance, int rewardPoints) {}
