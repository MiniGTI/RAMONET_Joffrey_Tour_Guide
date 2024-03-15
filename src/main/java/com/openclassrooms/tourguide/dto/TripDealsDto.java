package com.openclassrooms.tourguide.dto;

import lombok.Builder;
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

@Builder
public record TripDealsDto(UUID userId, List<Provider> providers) {}
