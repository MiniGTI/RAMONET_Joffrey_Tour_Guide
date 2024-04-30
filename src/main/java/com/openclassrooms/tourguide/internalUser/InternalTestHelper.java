package com.openclassrooms.tourguide.internalUser;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to manage the userTest generation.
 */
@Slf4j
public class InternalTestHelper {
    
    /**
     * Variable to set the number of user generated.
     * Set this default up to 100,000 for testing.
     */
    
    @Getter
    @Setter
    private static int internalUserNumber = 100;

    

}
