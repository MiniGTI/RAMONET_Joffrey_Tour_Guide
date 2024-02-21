package com.openclassrooms.tourguide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NearAttractionsListDto {
    
    private List<NearAttractionDto> nearAttractionList;
    
}
