package com.example.redisscanmgettool.controller;

import com.example.redisscanmgettool.redis.RedisScannerXyz;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RedisOperationsController {

    private static final Pattern ADID_REGEX = Pattern.compile("(?<=adid\":\")(.*?)(?=\"|$)");

    private final RedisScannerXyz redisScanner;
    private final RedisTemplate<String, byte[]> bytesRedisTemplate;
    private final Jedis jedis;

    @GetMapping(value = "/redis/scan", produces = APPLICATION_JSON_VALUE)
    List<String> scan(@RequestParam String keyPattern, @RequestParam(defaultValue = "100") int batchSize) {
        List<String> results = new ArrayList<>();
        redisScanner.scanAll(keyPattern, batchSize, results::addAll);
        return results;
    }

    @GetMapping(value = "/redis/dump", produces = APPLICATION_JSON_VALUE)
    ResponseEntity<Resource> scan(@RequestParam String keyPattern) {
        List<String> results = new ArrayList<>();
        redisScanner.scanAll(keyPattern, 1000, results::addAll);
        List<byte[]> values = bytesRedisTemplate.opsForValue().multiGet(results);
        if (values == null) {
            return ResponseEntity.notFound().build();
        }

        List<String> list = new ArrayList<>();
        values.forEach(it ->
            {
                String encoder = Base64.getEncoder().encodeToString(it);
                String decode = new String(Base64.getUrlDecoder().decode(encoder));
                Matcher matcher = ADID_REGEX.matcher(decode);
                if (matcher.find()) {
                    list.add(matcher.group());
                }
            });

        return ResponseEntity.ok().body(new InputStreamResource(new ByteArrayInputStream(String.join("\n", list).getBytes(UTF_8))));
    }

    @DeleteMapping(value = "/redis/delete")
    void clean() {
        jedis.flushAll();
    }
}
