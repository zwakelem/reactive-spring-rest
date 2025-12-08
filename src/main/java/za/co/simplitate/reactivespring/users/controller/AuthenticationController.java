package za.co.simplitate.reactivespring.users.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import za.co.simplitate.reactivespring.users.model.AuthenticationRequest;
import za.co.simplitate.reactivespring.users.service.AuthenticationService;

@RestController
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Void>> login(@RequestBody Mono<AuthenticationRequest> authenticationRequestMono) {
        return authenticationRequestMono
            .flatMap(authenticationRequest ->
                authenticationService.authenticate(authenticationRequest.getEmail(),
                    authenticationRequest.getPassword()))
                .map(authenticationResultMap -> ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationResultMap.get("token"))
                    .header("UserId", authenticationResultMap.get("userId"))
                    .build());
    }
}
