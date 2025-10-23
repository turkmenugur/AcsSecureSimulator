package com.acsteknoloji.simulator.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityLog {
    private Long logId;
    private String unitName;
    private String ipAddress;
    private String hostname;
    private String actionType;
    private String actionDetails;
    private LocalDateTime logTimestamp;
}