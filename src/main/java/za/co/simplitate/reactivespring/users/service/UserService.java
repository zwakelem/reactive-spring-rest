package za.co.simplitate.reactivespring.users.service;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import za.co.simplitate.reactivespring.users.controller.CreateUserRequest;
import za.co.simplitate.reactivespring.users.controller.UserRest;

import java.util.UUID;

public interface UserService {

    Mono<UserRest> createUser(Mono<CreateUserRequest>  createUserRequest);
    Mono<UserRest> getUserById(UUID id);
    Flux<UserRest> findAll(int page, int limit);


}
