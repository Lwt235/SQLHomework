package com.competition.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * Time service to manage system time
 * Supports test mode where time can be manually adjusted
 */
@Service
public class TimeService {
    
    // Test mode offset in seconds
    private Long timeOffsetSeconds = 0L;
    private boolean testMode = false;
    
    /**
     * Get current system time with test mode offset applied
     */
    public LocalDateTime getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        if (testMode && timeOffsetSeconds != 0) {
            return now.plusSeconds(timeOffsetSeconds);
        }
        return now;
    }
    
    /**
     * Enable test mode
     */
    public void enableTestMode() {
        this.testMode = true;
    }
    
    /**
     * Disable test mode and reset offset
     */
    public void disableTestMode() {
        this.testMode = false;
        this.timeOffsetSeconds = 0L;
    }
    
    /**
     * Check if test mode is enabled
     */
    public boolean isTestMode() {
        return testMode;
    }
    
    /**
     * Set time offset for test mode
     * @param offsetSeconds Number of seconds to offset from current time (can be negative)
     */
    public void setTimeOffset(Long offsetSeconds) {
        this.timeOffsetSeconds = offsetSeconds;
    }
    
    /**
     * Set test time directly
     * @param testTime The time to set as current time
     */
    public void setTestTime(LocalDateTime testTime) {
        LocalDateTime now = LocalDateTime.now();
        this.timeOffsetSeconds = java.time.Duration.between(now, testTime).getSeconds();
        this.testMode = true;
    }
    
    /**
     * Get current offset in seconds
     */
    public Long getTimeOffsetSeconds() {
        return timeOffsetSeconds;
    }
}
