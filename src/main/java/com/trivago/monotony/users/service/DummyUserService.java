package com.trivago.monotony.users.service;

import com.trivago.monotony.users.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class DummyUserService implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(DummyUserService.class);

    @Override
    public Mono<UserDetails> findUserById(String userId) {
        logger.info("SERVICE: Finding user {}", userId);
        return Mono.delay(Duration.ofMillis(150))
                .flatMap(d -> {
                    if ("user1".equals(userId)) {
                        return Mono.just(new UserDetails(userId, "Alice", "alice@example.com", "GOLD"));
                    } else if ("user-timeout".equals(userId)) {
                        return Mono.delay(Duration.ofSeconds(5))
                                .then(Mono.just(new UserDetails(userId, "Timeout User", "timeout@example.com", "SILVER")));
                    } else if ("user-error".equals(userId)) {
                        return Mono.error(new RuntimeException("Simulated user DB error for " + userId));
                    } else {
                        return Mono.empty(); // User not found
                    }
                });
    }
}
