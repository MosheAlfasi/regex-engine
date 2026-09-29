package com.noma.assignment.scan.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.noma.assignment.TestProps;
import com.noma.assignment.config.ScanProperties;
import com.noma.assignment.scan.api.dto.ScanRequest;
import com.noma.assignment.scan.api.dto.ScanResponse.PatternMatches;
import com.noma.assignment.scan.api.dto.FileSource;
import com.noma.assignment.scan.extract.ContentType;
import com.noma.assignment.scan.extract.NotebookExtractor;
import com.noma.assignment.scan.extract.RawTextExtractor;
import com.noma.assignment.scan.fetch.FileSystemContentFetcher;
import com.noma.assignment.scan.fetch.HttpContentFetcher;
import com.noma.assignment.scan.match.RegexMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

/** Wires the real strategies together to verify the pipeline end to end (filesystem only, no network). */
class ScanServiceTest {

    private static final String IMPORTS = "^\\s*(?:import|from)\\s+\\w+";

    @TempDir
    Path baseDir;

    private ScanService service;

    @BeforeEach
    void setUp() {
        ScanProperties props = TestProps.withBaseDir(baseDir);
        service = new ScanService(
                List.of(new FileSystemContentFetcher(props), new HttpContentFetcher(RestClient.create(), props)),
                List.of(new RawTextExtractor(), new NotebookExtractor(JsonMapper.builder().build())),
                new RegexMatcher(props));
    }

    @Test
    void sourceCode_scansWholeFile() throws Exception {
        Files.writeString(baseDir.resolve("a.py"), "import os\nfrom evil import x\nprint(1)");

        assertThat(service.scan(request("a.py", ContentType.RAW)).results())
                .containsExactly(new PatternMatches(IMPORTS, List.of("import os", "from evil")));
    }

    @Test
    void notebook_scansOnlyCellSources() throws Exception {
        Files.writeString(baseDir.resolve("n.ipynb"), """
                {"cells": [{"cell_type": "code", "source": ["import numpy\\n"],
                            "outputs": [{"output_type": "stream", "text": ["import leaked\\n"]}]}]}
                """);

        assertThat(service.scan(request("n.ipynb", ContentType.NOTEBOOK)).results())
                .containsExactly(new PatternMatches(IMPORTS, List.of("import numpy")));
    }

    private static ScanRequest request(String location, ContentType contentType) {
        return new ScanRequest(new FileSource(location, null), contentType, List.of(IMPORTS));
    }
}
