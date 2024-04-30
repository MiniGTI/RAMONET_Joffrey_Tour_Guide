package com.openclassrooms.tourguide;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.tourguide.controller.TourGuideController;
import com.openclassrooms.tourguide.dto.NearAttractionDto;
import com.openclassrooms.tourguide.dto.NearAttractionsListDto;
import com.openclassrooms.tourguide.dto.TripDealsDto;
import com.openclassrooms.tourguide.dto.UserLocationDto;
import com.openclassrooms.tourguide.dto.UserRewardsDto;
import com.openclassrooms.tourguide.service.DtoService;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import tripPricer.Provider;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TourGuideController.class)
public class TestController {
    
    @MockBean
    private DtoService dtoService;
    
    @Autowired
    private MockMvc mvc;
    
    @Autowired
    private ObjectMapper mapper;
    
    @Test
    void shouldReturnIndexTest() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Greetings from TourGuide!"))
                .andDo(print());
    }
    
    @Test
    void shouldReturnUserLocationTest() throws Exception {
        String username = "Test";
        UserLocationDto result = new UserLocationDto(UUID.randomUUID(), new Location(56.00, 81.12), new Date());
        
        when(dtoService.userLocationGenerator(username)).thenReturn(result);
        
        mvc.perform(get("/getLocation").param("userName", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(result.userId()
                        .toString()))
                .andDo(print());
    }
    
    @Test
    void shouldReturnNearbyAttractionsTest() throws Exception {
        String username = "Test";
        NearAttractionDto nearAttractionDto = new NearAttractionDto("Test", 55.00, 58.15, 48.25, 68.14, 25.19, 289);
        NearAttractionsListDto result = new NearAttractionsListDto(UUID.randomUUID(), List.of(nearAttractionDto));
        
        when(dtoService.nearAttractionsListGenerator(username)).thenReturn(result);
        
        mvc.perform(get("/getNearbyAttractions").param("userName", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(result.userId()
                        .toString()))
                .andExpect(jsonPath("$.nearAttractionList.size()").value(1))
                .andDo(print());
    }
    
    @Test
    void shouldReturnRewardsTest() throws Exception {
        String username = "Test";
        
        UserRewardsDto result = new UserRewardsDto(UUID.randomUUID(),
                List.of(new UserReward(new VisitedLocation(UUID.randomUUID(), new Location(45.02, 98.08), new Date()),
                        new Attraction("Attration", "City", "State", 45.03, 98.00))));
        
        when(dtoService.UserRewardsListGenerator(username)).thenReturn(result);
        
        mvc.perform(get("/getRewards").param("userName", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(result.userId().toString()))
                .andExpect(jsonPath("$.userRewards.size()").value(1))
                .andDo(print());
    }
    
    @Test
    void shouldReturnTripDealsTest() throws Exception{
        String username = "Test";
        
        TripDealsDto result = new TripDealsDto(UUID.randomUUID(), List.of(new Provider(UUID.randomUUID(), "Test", 12.24)));
  
        when(dtoService.TripDealListGenerator(username)).thenReturn(result);
        
        mvc.perform(get("/getTripDeals").param("userName", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(result.userId().toString()))
                .andExpect(jsonPath("$.providers.size()").value(1))
                .andDo(print());
    }
}
