package com.openclassrooms.tourguide.tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;

/**
 * Class to manage the tracker thread.
 */
@Slf4j
public class Tracker extends Thread {
    
    /**
     * Manage the interval time between two positions of all users.
     */
    private static final long trackingPollingInterval = TimeUnit.MINUTES.toSeconds(5);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final TourGuideService tourGuideService;
    private boolean stop = false;
    
    public Tracker(TourGuideService tourGuideService) {
        this.tourGuideService = tourGuideService;
        
        executorService.submit(this);
    }
    
    /**
     * Assures to shut down the Tracker thread
     */
    public void stopTracking() {
        stop = true;
        executorService.shutdownNow();
    }
    
    /**
     * Run the thread.
     * Use stopWatch to measure the Time Elapsed.
     * Get the current location of all users.
     *
     * @see TourGuideService#trackUserLocation(User).
     */
    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();
        while(true) {
            if(Thread.currentThread()
                    .isInterrupted() || stop) {
                log.debug("Tracker stopping");
                break;
            }
            
            List<User> users = tourGuideService.getAllUsers();
            log.debug("Begin Tracker. Tracking " + users.size() + " users.");
            stopWatch.start();
            users.forEach(u -> tourGuideService.trackUserLocation(u));
            stopWatch.stop();
            log.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
            stopWatch.reset();
            try {
                log.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(trackingPollingInterval);
            } catch(InterruptedException e) {
                break;
            }
        }
    }
}
