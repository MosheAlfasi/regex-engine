package com.noma.assignment;

import java.nio.file.Path;
import java.time.Duration;

import com.noma.assignment.config.ScanProperties;

public final class TestProps {

    private TestProps() {
    }

    public static ScanProperties withBaseDir(Path baseDir) {
        return new ScanProperties(
                new ScanProperties.Fs(baseDir),
                new ScanProperties.Http(Duration.ofSeconds(2), Duration.ofSeconds(2)),
                1024, 3, 50);
    }
}
