package com.biblioteca.service.impl;

import org.springframework.stereotype.Service;
import com.biblioteca.dto.SessionInfo;
import com.biblioteca.service.SessionManagementService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionManagementServiceImpl implements SessionManagementService {
    // Almacena sesiones por nombre de usuario -> Map<sessionId, sessionInfo>
    private final Map<String, Map<String, SessionInfo>> activeSessions = new ConcurrentHashMap<>();

    // Almacena sesiones revocadas para verificar en el filtro
    private final Set<String> revokedSessions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final int MAX_SESSIONS_PER_USER = 10;

    @Override
    public void registerSession(String username, String sessionId, String deviceInfo, String ipAddress,
            String location) {
        // Eliminar cualquier sesión revocada antes de registrar una nueva
        revokedSessions.remove(sessionId);

        Map<String, SessionInfo> userSessions = activeSessions.computeIfAbsent(username,
                k -> new ConcurrentHashMap<>());

        // Verificar si ya existe esta sesión exacta
        if (userSessions.containsKey(sessionId)) {
            // Actualizar la sesión existente
            userSessions.put(sessionId,
                    new SessionInfo(sessionId, deviceInfo, ipAddress, location, LocalDateTime.now()));
            return;
        }

        // Limitar el número máximo de sesiones
        if (userSessions.size() >= MAX_SESSIONS_PER_USER) {
            // Encontrar la sesión más antigua
            Map.Entry<String, SessionInfo> oldestSession = userSessions.entrySet().stream()
                    .min(Comparator.comparing(e -> e.getValue().lastActivity()))
                    .orElse(null);

            if (oldestSession != null) {
                // Revocar la sesión más antigua
                revokedSessions.add(oldestSession.getKey());
                userSessions.remove(oldestSession.getKey());
            }
        }

        // Registrar la nueva sesión
        userSessions.put(sessionId, new SessionInfo(sessionId, deviceInfo, ipAddress, location, LocalDateTime.now()));
    }

    @Override
    public List<SessionInfo> getUserSessions(String username) {
        Map<String, SessionInfo> userSessions = activeSessions.getOrDefault(username, Collections.emptyMap());
        return new ArrayList<>(userSessions.values());
    }

    @Override
    public boolean invalidateSession(String username, String sessionId) {
        Map<String, SessionInfo> userSessions = activeSessions.get(username);
        if (userSessions != null && userSessions.containsKey(sessionId)) {
            userSessions.remove(sessionId);
            // Marcar esta sesión como revocada
            revokedSessions.add(sessionId);
            return true;
        }
        return false;
    }

    @Override
    public int invalidateAllSessionsExcept(String username, String currentSessionId) {
        Map<String, SessionInfo> userSessions = activeSessions.get(username);
        if (userSessions != null) {
            int count = 0;
            Iterator<Map.Entry<String, SessionInfo>> iterator = userSessions.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SessionInfo> entry = iterator.next();
                if (!entry.getKey().equals(currentSessionId)) {
                    // Marcar esta sesión como revocada
                    revokedSessions.add(entry.getKey());
                    iterator.remove();
                    count++;
                }
            }
            return count;
        }
        return 0;
    }

    @Override
    public void updateLastActivity(String username, String sessionId) {
        Map<String, SessionInfo> userSessions = activeSessions.get(username);
        if (userSessions != null && userSessions.containsKey(sessionId)) {
            SessionInfo old = userSessions.get(sessionId);
            userSessions.put(sessionId,
                    new SessionInfo(old.id(), old.deviceInfo(), old.ipAddress(), old.location(), LocalDateTime.now()));
        }
    }

    @Override
    public boolean isSessionRevoked(String sessionId) {
        return sessionId != null && revokedSessions.contains(sessionId);
    }

    @Override
    public void purgeRevokedSession(String sessionId) {
        revokedSessions.remove(sessionId);
    }
}