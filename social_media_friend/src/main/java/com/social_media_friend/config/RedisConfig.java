package com.social_media_friend.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("localhost");
        config.setPort(6379);

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(20);
//        Xác định số lượng kết nối tối đa trong pool.
//        Nếu có nhiều hơn 20 yêu cầu kết nối cùng lúc, các yêu cầu vượt quá sẽ bị chặn hoặc từ chối.
        poolConfig.setMaxIdle(10);
//        Số lượng kết nối nhàn rỗi tối đa trong pool.
//        Nếu có hơn 10 kết nối không được sử dụng, chúng sẽ bị đóng để tiết kiệm tài nguyên.
        poolConfig.setMinIdle(5);
//        Số lượng kết nối nhàn rỗi tối thiểu.
//        Nếu số lượng kết nối nhàn rỗi giảm xuống dưới 5, pool sẽ tự động tạo thêm kết nối để đảm bảo hiệu suất.
        poolConfig.setMaxWait(Duration.ofMillis(1000));
//        Thời gian chờ tối đa khi yêu cầu một kết nối từ pool.
//        Nếu sau 1000ms mà không có kết nối khả dụng, yêu cầu sẽ bị từ chối hoặc gây lỗi.
        LettucePoolingClientConfiguration clientConfig =
                LettucePoolingClientConfiguration.builder()
                        .commandTimeout(Duration.ofSeconds(2))
                        .poolConfig(poolConfig)
                        .build();
        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

}
