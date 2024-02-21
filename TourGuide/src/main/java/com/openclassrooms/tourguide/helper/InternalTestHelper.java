package com.openclassrooms.tourguide.helper;

import lombok.Getter;
import lombok.Setter;

/**
 * Class to manage the userTest generation.
 */

public class InternalTestHelper {
	
	/**
	 * Variable to set the number of user generated.
	 * Set this default up to 100,000 for testing.
	 */
	@Getter
	@Setter
	private static int internalUserNumber = 100;
	
}
