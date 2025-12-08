package za.co.simplitate.reactivespring.users.service;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import za.co.simplitate.reactivespring.users.controller.CreateUserRequest;
import za.co.simplitate.reactivespring.users.controller.UserRest;
import za.co.simplitate.reactivespring.users.data.UserEntity;
import za.co.simplitate.reactivespring.users.data.UserRepository;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserRest> createUser(Mono<CreateUserRequest> createUserRequest) {
        return createUserRequest
                .mapNotNull(this::convertToUserEntity)
                .flatMap(userRepository::save)
                .mapNotNull(this::convertToUserRest);
    }

    /*@Override
    public Mono<UserRest> createUser(Mono<CreateUserRequest> createUserRequest) {
        return createUserRequest
            .mapNotNull(this::convertToUserEntity)
            .flatMap(userRepository::save)
            .mapNotNull(this::convertToUserRest)
            .onErrorMap(throwable -> {
                if(throwable instanceof DuplicateKeyException) {
                    return new ResponseStatusException(HttpStatus.CONFLICT, throwable.getMessage());
                } else if(throwable instanceof DataIntegrityViolationException) {
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, throwable.getMessage());
                } else {
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage());
                }
            });
    }*/

    @Override
    public Mono<UserRest> getUserById(UUID id) {
        return userRepository.findById(id)
                .mapNotNull(this::convertToUserRest);
    }

    @Override
    public Flux<UserRest> findAll(int page, int limit) {
        if(page > 0) page = page - 1;
        Pageable pageable = PageRequest.of(page, limit);
        return userRepository.findAllBy(pageable)
                .map(this::convertToUserRest);
    }


    private UserEntity convertToUserEntity(CreateUserRequest createUserRequest) {
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(createUserRequest, userEntity);
        return userEntity;
    }

    private UserRest convertToUserRest(UserEntity userEntity) {
        UserRest userRest = new UserRest();
        BeanUtils.copyProperties(userEntity, userRest);
        return userRest;
    }
}
