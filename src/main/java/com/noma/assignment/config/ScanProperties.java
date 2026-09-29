package com.noma.assignment.config;

import java.nio.file.Path;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scan")
public record ScanProperties(
        Fs fs,
        Http http,
        long maxContentBytes,
        int maxPatterns,
        int maxPatternLength) {

    public record Fs(Path baseDir) {
        public Fs {
            baseDir = baseDir.toAbsolutePath().normalize();
        }
    }

    public record Http(Duration connectTimeout, Duration readTimeout) {
    }
}
