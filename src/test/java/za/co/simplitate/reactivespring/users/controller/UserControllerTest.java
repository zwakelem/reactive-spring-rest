package za.co.simplitate.reactivespring.users.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import za.co.simplitate.reactivespring.users.infrastructure.TestSecurityConfig;
import za.co.simplitate.reactivespring.users.model.CreateUserRequest;
import za.co.simplitate.reactivespring.users.model.UserRest;
import za.co.simplitate.reactivespring.users.service.UserService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@WebFluxTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("create user with valid request, returns create status and user details")
    void testCreateUser() {

        CreateUserRequest createUserRequest = new CreateUserRequest(
                "user", "one", "user1@gmail.com", "password"
        );
        UUID userId = UUID.randomUUID();
        String expectedLocation = "/users/" + userId;
        UserRest expectedUserRest = new UserRest(userId, createUserRequest.getFirstName(),
                createUserRequest.getLastName(), createUserRequest.getEmail(), null);

        when(userService.createUser(any())).thenReturn(Mono.just(expectedUserRest));


        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location(expectedLocation)
                .expectBody(UserRest.class)
                .value(response -> {
                    assert response != null;
                    assertEquals(expectedUserRest.getId(), response.getId());
                    assertEquals(expectedUserRest.getFirstName(), response.getFirstName());
                    assertEquals(expectedUserRest.getLastName(), response.getLastName());
                    assertEquals(expectedUserRest.getEmail(), response.getEmail());
                });

        verify(userService, times(1)).createUser(any());
    }
}
