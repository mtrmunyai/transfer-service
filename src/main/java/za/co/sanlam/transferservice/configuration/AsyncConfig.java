package za.co.sanlam.transferservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean(name = "transferExecutor")
    public Executor transferExecutor() {
        return Executors.newFixedThreadPool(20);
    }
}
