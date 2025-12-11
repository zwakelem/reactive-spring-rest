package za.co.simplitate.reactivespring.users.data;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;


@DataR2dbcTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryTest {

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    void setUp() {
        UserEntity user1 = new UserEntity(UUID.randomUUID(),
                "user",
                "one",
                "user1@gmail.com",
                "password");
        UserEntity user2 = new UserEntity(UUID.randomUUID(),
                "user",
                "two",
                "user2@gmail.com",
                "password");
        UserEntity user3 = new UserEntity(UUID.randomUUID(),
                "user",
                "three",
                "user3@gmail.com",
                "password");

        String insertSql = "INSERT INTO users (id, first_name, last_name, email, password) VALUES " +
                "(:id, :firstName, :lastName, :email, :password)";
        Flux.just(user1, user2, user3)
                .concatMap(userEntity -> databaseClient.sql(insertSql)
                        .bind("id", userEntity.getId())
                        .bind("firstName", userEntity.getFirstName())
                        .bind("lastName", userEntity.getLastName())
                        .bind("email", userEntity.getEmail())
                        .bind("password", userEntity.getPassword())
                        .fetch()
                        .rowsUpdated())
                .then()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @AfterAll
    void tearDown() {
        databaseClient.sql("TRUNCATE TABLE users")
                .then()
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    @DisplayName("test findByEmail(), with an email that exists, returns matching user")
    void testFindByEmail() {
        String emailToFind = "user1@gmail.com";

        StepVerifier.create(userRepository.findByEmail(emailToFind))
                .expectNextMatches(user -> user.getEmail().equals(emailToFind))
                .verifyComplete();
    }

    @Test
    @DisplayName("test findByEmail(), email does not exists, returns empty mono")
    void testFindByEmail_negative() {
        String emailToFind = "notexists@gmail.com";

        StepVerifier.create(userRepository.findByEmail(emailToFind))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("test findAllBy(), with valid Pageable, returns paginated results")
    void testFindAllBy() {
        Pageable pageable = PageRequest.of(0, 2);

        StepVerifier.create(userRepository.findAllBy(pageable))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("test findAllBy(), with valid Pageable, returns one results")
    void testFindAllBy_negative() {
        Pageable pageable = PageRequest.of(1, 2);

        StepVerifier.create(userRepository.findAllBy(pageable))
                .expectNextCount(1)
                .verifyComplete();
    }
}
