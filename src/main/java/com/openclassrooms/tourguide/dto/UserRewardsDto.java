package com.openclassrooms.tourguide.dto;

import com.openclassrooms.tourguide.user.UserReward;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Dto model to meet the getRewards method into the TourGuideController.
 * <p>
 * Returned by UserRewardsListGenerator in the DtoService.
 * </p>
 *
 * @see com.openclassrooms.tourguide.service.DtoService#UserRewardsListGenerator(String)
 * @see com.openclassrooms.tourguide.controller.TourGuideController#getRewards(String)
 */
@Setter
@Getter
@Builder
public class UserRewardsDto {
    private final UUID userId;
    private List<UserReward> userRewards;
}
