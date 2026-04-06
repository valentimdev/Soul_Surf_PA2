package com.soulsurf.backend.core.security.ratelimit;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthRateLimitService {

    private static final class AttemptState {
        private int attempts;
        private Instant windowStart;
        private Instant blockedUntil;
    }

    private final ConcurrentHashMap<String, AttemptState> attemptsByKey = new ConcurrentHashMap<>();

    public boolean isBlocked(String key, int windowSeconds) {
        Instant now = Instant.now();
        AttemptState state = attemptsByKey.get(key);
        if (state == null) {
            return false;
        }

        synchronized (state) {
            normalizeWindow(state, now, windowSeconds);
            return state.blockedUntil != null && state.blockedUntil.isAfter(now);
        }
    }

    public void recordFailure(String key, int maxAttempts, int windowSeconds, int blockSeconds) {
        Instant now = Instant.now();
        AttemptState state = attemptsByKey.computeIfAbsent(key, ignored -> new AttemptState());

        synchronized (state) {
            normalizeWindow(state, now, windowSeconds);

            state.attempts += 1;
            if (state.attempts >= maxAttempts) {
                state.blockedUntil = now.plusSeconds(blockSeconds);
            }
        }
    }

    public void recordSuccess(String key) {
        attemptsByKey.remove(key);
    }

    private void normalizeWindow(AttemptState state, Instant now, int windowSeconds) {
        if (state.windowStart == null) {
            state.windowStart = now;
            return;
        }

        if (state.windowStart.plusSeconds(windowSeconds).isBefore(now)) {
            state.windowStart = now;
            state.attempts = 0;
            state.blockedUntil = null;
        }
    }
}
