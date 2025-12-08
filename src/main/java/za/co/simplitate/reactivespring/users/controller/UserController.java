package za.co.simplitate.reactivespring.users.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import za.co.simplitate.reactivespring.users.model.CreateUserRequest;
import za.co.simplitate.reactivespring.users.model.UserRest;
import za.co.simplitate.reactivespring.users.service.UserService;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<UserRest>> createUser(@RequestBody @Valid Mono<CreateUserRequest> user) {

        return userService.createUser(user)
                .map(userRest -> ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/users/" + userRest.getId()))
                .body(userRest));
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<UserRest>> getUserById(@PathVariable("userId") UUID userId) {
        return userService.getUserById(userId)
                .map(userRest -> ResponseEntity.status(HttpStatus.OK)
                .body(userRest))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));
    }

    @GetMapping
    public Flux<UserRest> getAllUsers(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                      @RequestParam(value = "limit", defaultValue = "50") int limit) {
        return userService.findAll(offset, limit);
    }
}
