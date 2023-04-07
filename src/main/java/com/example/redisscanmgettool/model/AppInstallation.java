package com.example.redisscanmgettool.model;

import lombok.Builder;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
@Builder
public class AppInstallation {

    String authSessionId;

    String appName;

    String feedRegion;

    String platform;

    @Builder.Default
    Map<String, String> appTrackingIds = new HashMap<>();
}
