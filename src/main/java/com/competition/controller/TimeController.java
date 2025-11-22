package com.competition.controller;

import com.competition.dto.ApiResponse;
import com.competition.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for system time management (test mode)
 * Admin only
 */
@RestController
@RequestMapping("/api/system/time")
@CrossOrigin(origins = "*")
public class TimeController {
    
    @Autowired
    private TimeService timeService;
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Get current system time
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentTime() {
        Map<String, Object> data = new HashMap<>();
        data.put("currentTime", timeService.getCurrentTime().format(formatter));
        data.put("testMode", timeService.isTestMode());
        data.put("offsetSeconds", timeService.getTimeOffsetSeconds());
        data.put("realTime", LocalDateTime.now().format(formatter));
        return ResponseEntity.ok(ApiResponse.success("Current time retrieved", data));
    }
    
    /**
     * Enable test mode
     */
    @PostMapping("/test-mode/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> enableTestMode() {
        timeService.enableTestMode();
        return ResponseEntity.ok(ApiResponse.success("测试模式已启用", null));
    }
    
    /**
     * Disable test mode
     */
    @PostMapping("/test-mode/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> disableTestMode() {
        timeService.disableTestMode();
        return ResponseEntity.ok(ApiResponse.success("测试模式已禁用", null));
    }
    
    /**
     * Set test time
     */
    @PostMapping("/test-mode/set")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setTestTime(@RequestBody Map<String, String> payload) {
        try {
            String timeStr = payload.get("testTime");
            if (timeStr == null || timeStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("测试时间不能为空"));
            }
            
            LocalDateTime testTime = LocalDateTime.parse(timeStr, formatter);
            timeService.setTestTime(testTime);
            
            Map<String, Object> data = new HashMap<>();
            data.put("testTime", testTime.format(formatter));
            data.put("currentTime", timeService.getCurrentTime().format(formatter));
            data.put("testMode", timeService.isTestMode());
            
            return ResponseEntity.ok(ApiResponse.success("测试时间已设置", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("设置测试时间失败: " + e.getMessage()));
        }
    }
    
    /**
     * Set time offset in seconds
     */
    @PostMapping("/test-mode/offset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setTimeOffset(@RequestBody Map<String, Long> payload) {
        try {
            Long offsetSeconds = payload.get("offsetSeconds");
            if (offsetSeconds == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("时间偏移量不能为空"));
            }
            
            timeService.enableTestMode();
            timeService.setTimeOffset(offsetSeconds);
            
            Map<String, Object> data = new HashMap<>();
            data.put("offsetSeconds", offsetSeconds);
            data.put("currentTime", timeService.getCurrentTime().format(formatter));
            data.put("realTime", LocalDateTime.now().format(formatter));
            
            return ResponseEntity.ok(ApiResponse.success("时间偏移已设置", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("设置时间偏移失败: " + e.getMessage()));
        }
    }
}
