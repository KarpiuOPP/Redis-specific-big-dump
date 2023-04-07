package com.example.redisscanmgettool.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
@RequiredArgsConstructor
public class RedisScannerXyz {

    private final StringRedisTemplate redisTemplate;

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void scanAll(String pattern, int batchSize, KeysProcessors onKeys) {
        ScanOptions scanOptions = ScanOptions.scanOptions().match(pattern).count(batchSize).build();
        try (
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            Cursor<byte[]> cursor = connection.scan(scanOptions)
        ) {
            List<String> keys = new ArrayList<>(batchSize);
            while (cursor.hasNext()) {
                String key = new String(cursor.next(), UTF_8);
                keys.add(key);
                if (keys.size() == batchSize) {
                    onKeys.process(List.copyOf(keys));
                    keys.clear();
                }
            }
            onKeys.process(List.copyOf(keys));
        }
    }

    @FunctionalInterface
    public interface KeysProcessors {

        void process(List<String> keys);
    }
}
