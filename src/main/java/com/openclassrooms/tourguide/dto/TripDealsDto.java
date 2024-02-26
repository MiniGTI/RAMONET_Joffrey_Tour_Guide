package com.openclassrooms.tourguide.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import tripPricer.Provider;

import java.util.List;
import java.util.UUID;

/**
 * Dto model to meet the getTripDeals method into the TourGuideController.
 * <p>
 * Returned by TripDealListGenerator in the DtoService.
 * </p>
 *
 * @see com.openclassrooms.tourguide.service.DtoService#TripDealListGenerator(String)
 * @see com.openclassrooms.tourguide.controller.TourGuideController#getTripDeals(String)
 */
@Setter
@Getter
@Builder
public class TripDealsDto {
    
    private final UUID userId;
    private final List<Provider> providers;
}
