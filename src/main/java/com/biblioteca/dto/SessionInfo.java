package com.biblioteca.dto;

import java.time.LocalDateTime;

public record SessionInfo(
        String id, String deviceInfo, String ipAddress, String location, LocalDateTime lastActivity) {

}
