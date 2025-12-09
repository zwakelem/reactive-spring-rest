package za.co.simplitate.reactivespring.users.service;

import reactor.core.publisher.Mono;

public interface JwtService {

    String generateJwtToken(String subject);
    Mono<Boolean> validateJwtToken(String token);
    String extractTokenSubject(String token);
}
