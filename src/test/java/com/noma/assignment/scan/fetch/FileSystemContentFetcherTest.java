package com.noma.assignment.scan.fetch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.noma.assignment.TestProps;
import com.noma.assignment.common.exceptions.AppException;
import com.noma.assignment.common.exceptions.ErrorType;
import com.noma.assignment.scan.api.dto.FileSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FileSystemContentFetcherTest {

    @TempDir
    Path baseDir;

    private FileSystemContentFetcher fetcher;

    @BeforeEach
    void setUp() {
        fetcher = new FileSystemContentFetcher(TestProps.withBaseDir(baseDir));
    }

    @Test
    void readsUtf8ByDefault() throws Exception {
        Files.writeString(baseDir.resolve("a.py"), "import os # café", StandardCharsets.UTF_8);

        assertThat(fetcher.fetch(file("a.py", null))).isEqualTo("import os # café");
    }

    @ParameterizedTest
    @ValueSource(strings = {"latin-1", "ISO-8859-1", "utf-16", "UTF-16LE"})
    void decodesWithRequestedEncoding(String encoding) throws Exception {
        String canonical = encoding.equals("latin-1") ? "ISO-8859-1" : encoding;
        Files.write(baseDir.resolve("a.py"), "import os # café".getBytes(canonical));

        assertThat(fetcher.fetch(file("a.py", encoding))).isEqualTo("import os # café");
    }

    @Test
    void unknownEncoding_rejected() throws Exception {
        Files.writeString(baseDir.resolve("a.py"), "x");

        assertThatThrownBy(() -> fetcher.fetch(file("a.py", "klingon")))
                .isInstanceOf(AppException.class)
                .extracting("type").isEqualTo(ErrorType.INVALID_ARGUMENT);
    }

    @Test
    void missingFile_notFound() {
        assertThatThrownBy(() -> fetcher.fetch(file("nope.py", null)))
                .isInstanceOf(AppException.class)
                .extracting("type").isEqualTo(ErrorType.NOT_FOUND);
    }

    @ParameterizedTest
    @ValueSource(strings = {"../outside.py", "/etc/passwd", "sub/../../outside.py"})
    void pathOutsideBaseDir_rejected(String location) {
        assertThatThrownBy(() -> fetcher.fetch(file(location, null)))
                .isInstanceOf(AppException.class)
                .extracting("type").isEqualTo(ErrorType.INVALID_ARGUMENT);
    }

    @Test
    void fileOverSizeLimit_rejected() throws Exception {
        Files.writeString(baseDir.resolve("big.py"), "x".repeat(2048));

        assertThatThrownBy(() -> fetcher.fetch(file("big.py", null)))
                .isInstanceOf(AppException.class)
                .extracting("type").isEqualTo(ErrorType.CONTENT_TOO_LARGE);
    }

    private static FileSource file(String path, String encoding) {
        return new FileSource(path, encoding);
    }
}
