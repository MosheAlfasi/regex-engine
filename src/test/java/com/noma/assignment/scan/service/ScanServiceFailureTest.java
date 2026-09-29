package com.noma.assignment.scan.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.List;

import com.noma.assignment.TestProps;
import com.noma.assignment.common.exceptions.AppException;
import com.noma.assignment.common.exceptions.ErrorType;
import com.noma.assignment.scan.api.dto.FileSource;
import com.noma.assignment.scan.api.dto.ScanRequest;
import com.noma.assignment.scan.api.dto.SourceDTO;
import com.noma.assignment.scan.extract.ContentExtractor;
import com.noma.assignment.scan.extract.ContentType;
import com.noma.assignment.scan.extract.RawTextExtractor;
import com.noma.assignment.scan.fetch.ContentFetcher;
import com.noma.assignment.scan.match.RegexMatcher;
import org.junit.jupiter.api.Test;

class ScanServiceFailureTest {

    private static final ScanRequest REQUEST =
            new ScanRequest(new FileSource("a.py", null), ContentType.RAW, List.of("x"));

    private final RegexMatcher matcher = new RegexMatcher(TestProps.withBaseDir(Path.of(".")));

    @Test
    void unexpectedFetcherFailure_isWrappedAsFetchFailed_withCause() {
        ScanService service = new ScanService(List.of(throwingFetcher()), List.of(new RawTextExtractor()), matcher);

        assertThatThrownBy(() -> service.scan(REQUEST))
                .isInstanceOf(AppException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .extracting("type").isEqualTo(ErrorType.FETCH_FAILED);
    }

    @Test
    void unexpectedExtractorFailure_isWrappedAsExtractionFailed_withCause() {
        ScanService service = new ScanService(List.of(fetcherReturning("x")), List.of(throwingExtractor()), matcher);

        assertThatThrownBy(() -> service.scan(REQUEST))
                .isInstanceOf(AppException.class)
                .hasCauseInstanceOf(NullPointerException.class)
                .extracting("type").isEqualTo(ErrorType.EXTRACTION_FAILED);
    }

    @Test
    void appExceptionFromFetcher_isPropagatedUnchanged() {
        AppException original = new AppException(ErrorType.NOT_FOUND, "File", "a.py");
        ContentFetcher fetcher = new StubFetcher() {
            @Override
            public String fetch(SourceDTO source) {
                throw original;
            }
        };
        ScanService service = new ScanService(List.of(fetcher), List.of(new RawTextExtractor()), matcher);

        assertThatThrownBy(() -> service.scan(REQUEST)).isSameAs(original);
    }

    @Test
    void noFetcherSupportsSource_unsupported() {
        ScanService service = new ScanService(List.of(), List.of(new RawTextExtractor()), matcher);

        assertThatThrownBy(() -> service.scan(REQUEST))
                .extracting("type").isEqualTo(ErrorType.UNSUPPORTED_SOURCE);
    }

    @Test
    void happyPath_stillWorks() {
        ScanService service = new ScanService(List.of(fetcherReturning("x y x")), List.of(new RawTextExtractor()), matcher);

        assertThat(service.scan(REQUEST).results().get(0).matches()).containsExactly("x", "x");
    }

    private abstract static class StubFetcher implements ContentFetcher {
        @Override
        public boolean supports(SourceDTO source) {
            return true;
        }
    }

    private static ContentFetcher throwingFetcher() {
        return new StubFetcher() {
            @Override
            public String fetch(SourceDTO source) {
                throw new IllegalStateException("boom");
            }
        };
    }

    private static ContentFetcher fetcherReturning(String content) {
        return new StubFetcher() {
            @Override
            public String fetch(SourceDTO source) {
                return content;
            }
        };
    }

    private static ContentExtractor throwingExtractor() {
        return new ContentExtractor() {
            @Override
            public boolean supports(ContentType contentType) {
                return true;
            }

            @Override
            public List<String> extract(String content) {
                throw new NullPointerException("boom");
            }
        };
    }
}
