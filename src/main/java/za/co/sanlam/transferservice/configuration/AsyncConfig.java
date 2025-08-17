package za.co.sanlam.transferservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

  @Bean(name = "transferExecutor")
  public Executor transferExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10); // adjust based on load
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("transfer-exec-");
    executor.initialize();
    return executor;
  }
}
