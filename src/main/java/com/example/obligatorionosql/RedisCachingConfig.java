package com.example.obligatorionosql;

import javax.annotation.PreDestroy;

import lombok.RequiredArgsConstructor;

import models.Comentario;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class RedisCachingConfig extends CachingConfigurerSupport {

    private final RedisConnectionFactory connectionFactory;

    public RedisCachingConfig(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }



    @Bean
    public ReactiveRedisTemplate<String, Comentario> reactiveJsonBookRedisTemplate(
            ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {

        RedisSerializationContext<String, Comentario> serializationContext = RedisSerializationContext
                .<String, Comentario>newSerializationContext(new StringRedisSerializer()).hashKey(new StringRedisSerializer())
                .hashValue(new Jackson2JsonRedisSerializer<>(Comentario.class)).build();

        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
    }

    @PreDestroy
    public void flushTestDb() {
        this.connectionFactory.getConnection().flushDb();
    }

}