package com.openclassrooms.tourguide.helper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Class to manage the userTest generation.
 */
@Slf4j
@Service
public class InternalTestHelper {
    
    /**
     * Variable to set the number of user generated.
     * Set this default up to 100,000 for testing.
     */
    @Setter
    @Getter
    private static int internalUserNumber = 100;

}
