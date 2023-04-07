package com.example.redisscanmgettool;

import com.example.redisscanmgettool.model.AppInstallation;
import com.example.redisscanmgettool.redis.RedisConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.UUID;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(RedisConfig.class);

        RedisTemplate<String, AppInstallation> redisJsonTemplate = context.getBean("redisJsonTemplate", RedisTemplate.class);

        for(int i = 0; i++ < 100; i++) {
            String authSessionId = UUID.randomUUID().toString();
            AppInstallation appInstallation = createAppInstallation(authSessionId);
            redisJsonTemplate.boundValueOps("appInstallation|" + authSessionId).set(appInstallation);
        }
    }

    private static AppInstallation createAppInstallation(String authSessionId) {
       return AppInstallation.builder()
           .authSessionId(authSessionId)
           .appName("bet")
           .feedRegion("US")
           .platform("web")
           .appTrackingIds(Map.of(
               "adid", UUID.randomUUID().toString(),
               "idfa", UUID.randomUUID().toString()
           ))
           .build();
    }

}
