package com.openclassrooms.tourguide.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
