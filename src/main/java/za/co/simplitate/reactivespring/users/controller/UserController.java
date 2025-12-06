package za.co.simplitate.reactivespring.users.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    /*@PostMapping
    public Mono<UserRest> createUser1(@RequestBody @Valid Mono<CreateUserRequest> user) {
//        UserRest userRest = new UserRest();
//        return Mono.just(userRest);

        return user.map(request -> new UserRest(UUID.randomUUID(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail()));
    }*/

    /*@PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserRest> createUser2(@RequestBody @Valid Mono<CreateUserRequest> user) {
        return user.map(request -> new UserRest(UUID.randomUUID(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail()));
    }*/


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<UserRest>> createUser2(@RequestBody @Valid Mono<CreateUserRequest> user) {
        return user.map(request -> new UserRest(UUID.randomUUID(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail()))
            .map(userRest -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .location(URI.create("/users/" + userRest.getId()))
                    .body(userRest));
    }

    @GetMapping("/{userId}")
    public Mono<UserRest> getUserById(@PathVariable("userId") UUID userId) {
        return Mono.just(new UserRest(
                userId, "Simphy", "Mgabhi", "simphy@gmail.com"));
    }

    @GetMapping
    public Flux<UserRest> getAllUsers(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                      @RequestParam(value = "limit", defaultValue = "50") int limit) {
        return Flux.just(
                new UserRest(UUID.randomUUID(), "Simphy", "Mgabhi", "simphy@gmail.com"),
                new UserRest(UUID.randomUUID(), "Sibo", "Mgabhi", "sibo@gmail.com")
        );
    }
}
