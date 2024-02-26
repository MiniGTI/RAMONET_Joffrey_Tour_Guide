package com.openclassrooms.tourguide.dto;

import gpsUtil.location.Location;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

/**
 * Dto model to meet the getLocation method into the TourGuideController.
 * <p>
 * Returned by userLocationGenerator in the DtoService.
 * </p>
 *
 * @see com.openclassrooms.tourguide.service.DtoService#userLocationGenerator(String)
 * @see com.openclassrooms.tourguide.controller.TourGuideController#getLocation(String)
 */
@Setter
@Getter
@Builder
public class UserLocationDto {
    
    private UUID userID;
    private Location location;
    private Date timeVisited;
}
