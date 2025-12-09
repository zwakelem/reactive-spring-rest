package za.co.simplitate.reactivespring.users.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;
import za.co.simplitate.reactivespring.users.model.UserRest;

@Configuration
public class SinksConfig {

    @Bean
    public Sinks.Many<UserRest> userSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }
}
