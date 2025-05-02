package com.trivago.monotony.users.service;

import com.trivago.monotony.users.UserDetails;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserDetails> findUserById(String userId);
}
