package com.flashflow.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    private static final Logger log = LoggerFactory.getLogger(RedissonConfig.class);

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${flashflow.redis.enabled:true}")
    private boolean redisEnabled;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        if (!redisEnabled) {
            log.info("[Redis] Redis is disabled by configuration (flashflow.redis.enabled=false). Operating in In-Memory mode.");
            return null;
        }

        try {
            Config config = new Config();
            String address = String.format("redis://%s:%d", redisHost, redisPort);
            
            var singleServerConfig = config.useSingleServer()
                    .setAddress(address)
                    .setConnectTimeout(1500)
                    .setTimeout(3000)
                    .setRetryAttempts(2)
                    .setRetryInterval(500);

            if (redisPassword != null && !redisPassword.trim().isEmpty()) {
                singleServerConfig.setPassword(redisPassword);
            }

            RedissonClient client = Redisson.create(config);
            // Verify connectivity with ping
            client.getKeys().count();
            log.info("[Redis] Successfully connected to Redis distributed cluster at {}", address);
            return client;
        } catch (Exception e) {
            log.warn("[Redis] Could not connect to Redis at {}:{}. Falling back to In-Memory Lock Coordinator: {}", 
                    redisHost, redisPort, e.getMessage());
            return null;
        }
    }
}
