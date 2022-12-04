package com.wei.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedissonConfig {

    @Value("${spring.redis.host}")
    private String redisHost;
    @Bean
    public RedissonClient getRedissonClient(){
        // 默认连接地址 127.0.0.1:6379
        Config config = new Config();
        config.useSingleServer().setAddress("redis://"+redisHost+":6379");
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }

}
