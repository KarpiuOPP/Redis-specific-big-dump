package com.example.redisscanmgettool.redis;


import com.example.redisscanmgettool.model.AppInstallation;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JsonSerializer implements RedisSerializer<AppInstallation> {

    private final ObjectMapper mapper;

    @SneakyThrows
    @Override
    public byte[] serialize(AppInstallation appInstallation) throws SerializationException {
        return mapper.writeValueAsString(appInstallation).getBytes(StandardCharsets.UTF_8);
    }

    @SneakyThrows
    @Override
    public AppInstallation deserialize(byte[] bytes) throws SerializationException {
        return null;
    }
}
