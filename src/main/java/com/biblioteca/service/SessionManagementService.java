package com.biblioteca.service;

import java.util.List;

import com.biblioteca.dto.SessionInfo;

public interface SessionManagementService {
  void registerSession(String username, String sessionId, String deviceInfo, String ipAddress, String location);
  List<SessionInfo> getUserSessions(String username);
  boolean invalidateSession(String username, String sessionId);
  int invalidateAllSessionsExcept(String username, String currentSessionId);
  void updateLastActivity(String username, String sessionId);
  boolean isSessionRevoked(String sessionId);
  void purgeRevokedSession(String sessionId);
}
