package com.openclassrooms.tourguide.helper;

/**
 * Class to manage the userTest generation.
 */
public class InternalTestHelper {
	
	/**
	 * Variable to set the number of user generated.
	 * Set this default up to 100,000 for testing.
	 */
	private static int internalUserNumber = 100;
	
	public static void setInternalUserNumber(int internalUserNumber) {
		InternalTestHelper.internalUserNumber = internalUserNumber;
	}
	
	public static int getInternalUserNumber() {
		return internalUserNumber;
	}
}
