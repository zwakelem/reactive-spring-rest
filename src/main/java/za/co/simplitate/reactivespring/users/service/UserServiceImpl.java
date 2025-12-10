package za.co.simplitate.reactivespring.users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import za.co.simplitate.reactivespring.users.data.UserEntity;
import za.co.simplitate.reactivespring.users.data.UserRepository;
import za.co.simplitate.reactivespring.users.model.AlbumRest;
import za.co.simplitate.reactivespring.users.model.CreateUserRequest;
import za.co.simplitate.reactivespring.users.model.UserRest;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Sinks.Many<UserRest> userSink;
    private final WebClient webClient;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           Sinks.Many<UserRest> userSink,
                           WebClient webClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSink = userSink;
        this.webClient = webClient;
    }

    @Override
    public Mono<UserRest> createUser(Mono<CreateUserRequest> createUserRequest) {
        return createUserRequest
                .flatMap(this::convertToUserEntity)
                .flatMap(userRepository::save)
                .mapNotNull(this::convertToUserRest)
                .doOnSuccess(savedUser -> userSink.tryEmitNext(savedUser));
    }

    @Override
    public Mono<UserRest> getUserById(UUID id, String include, String jwt) {
        return userRepository.findById(id)
                .mapNotNull(this::convertToUserRest)
                .flatMap(userRest -> {
                    if(include != null && include.equals("albums")) {
                        // fetch user's photo albums
                        return fetchUserAlbums(userRest, jwt);
                    }
                    return Mono.just(userRest);
                });
    }

    private Mono<UserRest> fetchUserAlbums(UserRest userRest, String jwt) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .port(8084)
                        .path("/albums")
//                        .queryParam("userId", userRest.getId())
                        .build())
                .header("Authorization", jwt)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    return Mono.error(new RuntimeException("Albums not found for user"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    return Mono.error(new RuntimeException("Server error while fetching albums"));
                })
                .bodyToFlux(AlbumRest.class)
                .collectList()
                .map(albums -> {
                    userRest.setAlbums(albums);
                    return userRest;
                })
                .onErrorResume(e -> {
                    log.error("Error fetching albums", e);
                    return Mono.just(userRest);
                });
    }

    @Override
    public Flux<UserRest> findAll(int page, int limit) {
        if(page > 0) page = page - 1;
        Pageable pageable = PageRequest.of(page, limit);
        return userRepository.findAllBy(pageable)
                .map(this::convertToUserRest);
    }

    @Override
    public Flux<UserRest> streamUser() {
        return userSink.asFlux()
                .publish()
                .autoConnect(1);
    }


    private Mono<UserEntity> convertToUserEntity(CreateUserRequest createUserRequest) {
        return Mono.fromCallable(() -> {
            UserEntity userEntity = new UserEntity();
            BeanUtils.copyProperties(createUserRequest, userEntity);
            userEntity.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
            return userEntity;
        }).subscribeOn(Schedulers.boundedElastic());

    }

    private UserRest convertToUserRest(UserEntity userEntity) {
        UserRest userRest = new UserRest();
        BeanUtils.copyProperties(userEntity, userRest);
        return userRest;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByEmail(username)
            .map(userEntity -> User
                .withUsername(userEntity.getEmail())
                .password(userEntity.getPassword())
                .authorities(new ArrayList<>())
                .build());
    }
}
