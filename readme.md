# RAMONET-Joffrey_Tour_Guide
An application to facilitate travel planning.
This application uses Java and SpringBoot to test if the backend can support 100.000 users in the same time.
***
## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.
***
### Prerequisites
What things you need to install the software and how to install them.

    - Java 21.0.1  
    - Maven 3.2.5  
    - JUnit 5

### Running Application
Post installation of Java and Maven, you must import the following libraries to simulate APIs.

    - gpsUtil.jar
    - RewardCentral.jar
    - TripPricer.jar

They are in the root project folder. To install them, open the IDE's console and run the following instructions:

    - mvn install:install-file -Dfile=./libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar  
    - mvn install:install-file -Dfile=./libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar 
    - mvn install:install-file -Dfile=./libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar

There are no Databases, and the Tomcat server is running with a random port. If you want to assign it a static port, modify the `server.port` in the `application.properties`.


### Tests

They are many tests classes for unit and integration. And one class `TestPerformance` for test the application with a parametarize number of concurrent user.

#### TestPerformance

The first test `highVolumeTrackLocation` simulates the gps tracking of a user volume. 
The second test `highVolumeGetRewards` simulates the rewards calculation of a user volume.

To set the user volume, you must modify the following statement in the class’s `setUp` method.

    - InternalTestHelper.setInternalUserNumber("THE NUMBER OF USER");
