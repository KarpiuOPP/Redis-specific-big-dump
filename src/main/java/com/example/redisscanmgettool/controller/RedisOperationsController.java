package com.example.redisscanmgettool.controller;

import com.example.redisscanmgettool.model.AppInstallation;
import com.example.redisscanmgettool.redis.RedisScannerXyz;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RedisOperationsController {

    private final RedisScannerXyz redisScanner;
    private final RedisTemplate<String, byte[]> bytesRedisTemplate;
    private final RedisTemplate<String, AppInstallation> redisJsonTemplate;

    @GetMapping(value = "/redis/scan", produces = APPLICATION_JSON_VALUE)
    List<String> scan(@RequestParam String keyPattern, @RequestParam(defaultValue = "100") int batchSize) {
        List<String> results = new ArrayList<>();
        redisScanner.scanAll(keyPattern, batchSize, results::addAll);
        return results;
    }

    @GetMapping(value = "/redis/dump", produces = APPLICATION_JSON_VALUE)
    ResponseEntity<Resource> dumpViaVM(@RequestParam String keyPattern) {
        List<String> results = new ArrayList<>();
        redisScanner.scanAll(keyPattern, 1000, results::addAll);
        return ResponseEntity.ok().body(new InputStreamResource(new ByteArrayInputStream(String.join("\n", results).getBytes(UTF_8))));
    }

    @SneakyThrows
    @GetMapping(value = "/redis/dumpfile", produces = APPLICATION_JSON_VALUE)
    ResponseEntity<?> dumpToLocalFile(@RequestParam String keyPattern) {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();
        BufferedWriter writer = Files.newBufferedWriter(Paths.get("/tmp/sample.csv"));
        CSVPrinter printer = new CSVPrinter(writer, csvFormat);

        redisScanner.scanAll(keyPattern, 10_000, keys -> {
            System.err.println("Downloaded");
            List<AppInstallation> values = redisJsonTemplate.opsForValue().multiGet(keys);

            values.forEach(it -> {
                try {
                    String adid = it.getAppTrackingIds().get("adid");
                    printer.printRecord(adid);
                } catch (IOException e) {
                    System.err.println("Exception: " + e);
                    e.printStackTrace();
                }
            });
            try {
                printer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        printer.close();
        return ResponseEntity.ok().body("And it's gone");
    }


    @GetMapping(value = "/redis/get", produces = APPLICATION_JSON_VALUE)
    ResponseEntity<?> get(@RequestParam String key) {
        byte[] value = bytesRedisTemplate.opsForValue().get(key);
        if (value == null) {
            return ResponseEntity.notFound().build();
        }
        String base64 = Base64.getEncoder().encodeToString(value);
        return ResponseEntity.ok(Map.of("data", base64));
    }
}
