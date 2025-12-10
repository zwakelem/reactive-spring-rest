package za.co.simplitate.reactivespring.users.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import za.co.simplitate.reactivespring.users.data.UserEntity;
import za.co.simplitate.reactivespring.users.data.UserRepository;
import za.co.simplitate.reactivespring.users.model.CreateUserRequest;
import za.co.simplitate.reactivespring.users.model.UserRest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private WebClient webClient;

    private Sinks.Many<UserRest> userSink;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userSink = Sinks.many().multicast().onBackpressureBuffer();
        userService = new UserServiceImpl(userRepository, passwordEncoder, userSink, webClient);
    }

    @Test
    @DisplayName("test createUser() with valid request, return created user details")
    void createUser() {
        CreateUserRequest createUserRequest = new CreateUserRequest(
                "user", "one", "user1@gmail.com", "password"
        );
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        UserEntity savedEntity = new UserEntity(UUID.randomUUID(), createUserRequest.getFirstName(),
                createUserRequest.getLastName(), createUserRequest.getEmail(), null);
        when(userRepository.save(any())).thenReturn(Mono.just(savedEntity));

        Mono<UserRest> result = userService.createUser(Mono.just(createUserRequest));

        StepVerifier.create(result)
                .expectNextMatches(userRest -> userRest.getId().equals(savedEntity.getId())
                    && userRest.getFirstName().equals(savedEntity.getFirstName())
                    && userRest.getLastName().equals(savedEntity.getLastName())
                    && userRest.getEmail().equals(savedEntity.getEmail()))
                .verifyComplete();
        verify(userRepository, times(1)).save(any());

//        Below is blocking code
//        UserRest userRest = result.block();
//        assertEquals(savedEntity.getId(), userRest.getId());
//        assertEquals(savedEntity.getFirstName(), userRest.getFirstName());

    }

    @Test
    @DisplayName("test createUser(), with valid request, emits event to sink")
    void testCreateUser_withValidRequest_EmitsEventToSink() {
        // Arrange
        CreateUserRequest createUserRequest = new CreateUserRequest(
                "user", "one", "user1@gmail.com", "password"
        );
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        UserEntity savedEntity = new UserEntity(UUID.randomUUID(), createUserRequest.getFirstName(),
                createUserRequest.getLastName(), createUserRequest.getEmail(), null);
        when(userRepository.save(any())).thenReturn(Mono.just(savedEntity));

        // Subscribe to the sink before triggering the service call.
        Flux<UserRest> sinkFlux = userSink.asFlux();

        StepVerifier.create(
                        userService.createUser(Mono.just(createUserRequest))
                                .thenMany(userSink.asFlux().take(1))
                )
                .expectNextMatches(userRest ->
                        userRest.getId().equals(savedEntity.getId()) &&
                                userRest.getFirstName().equals(savedEntity.getFirstName()) &&
                                userRest.getLastName().equals(savedEntity.getLastName()) &&
                                userRest.getEmail().equals(savedEntity.getEmail())
                )
                .verifyComplete();
    }

}
