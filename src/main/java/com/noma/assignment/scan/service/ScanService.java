package com.noma.assignment.scan.service;

import java.util.List;
import java.util.regex.Pattern;

import com.noma.assignment.common.exceptions.AppException;
import com.noma.assignment.common.exceptions.ErrorType;
import com.noma.assignment.scan.api.dto.ScanRequest;
import com.noma.assignment.scan.api.dto.ScanResponse;
import com.noma.assignment.scan.api.dto.SourceDTO;
import com.noma.assignment.scan.extract.ContentExtractor;
import com.noma.assignment.scan.extract.ContentType;
import com.noma.assignment.scan.fetch.ContentFetcher;
import com.noma.assignment.scan.match.RegexMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Scan pipeline: fetch → extract → match.
 * Supporting a new source or content type only requires a new {@link ContentFetcher} / {@link ContentExtractor} bean.
 */
@Service
@RequiredArgsConstructor
public class ScanService {

    private final List<ContentFetcher> fetchers;
    private final List<ContentExtractor> extractors;
    private final RegexMatcher regexMatcher;

    public ScanResponse scan(ScanRequest request) {
        List<Pattern> patterns = regexMatcher.compile(request.patterns());

        // Q
        String content = fetch(request.source());

        List<String> chunks = extract(request.contentType(), content);

        // Q
        return new ScanResponse(regexMatcher.match(patterns, chunks));
    }

    private ContentFetcher findFetcher(SourceDTO source) {
        return fetchers.stream()
                .filter(f -> f.supports(source))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorType.UNSUPPORTED_SOURCE, "source", source.getClass().getSimpleName()));
    }

    private ContentExtractor findExtractor(ContentType contentType) {
        return extractors.stream()
                .filter(e -> e.supports(contentType))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorType.UNSUPPORTED_SOURCE, "content type", contentType));
    }

    private String fetch(SourceDTO source) {
        ContentFetcher fetcher = findFetcher(source);
        try {
            return fetcher.fetch(source);
        } catch (AppException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AppException(ErrorType.FETCH_FAILED, e, source.getClass().getSimpleName());
        }
    }

    private List<String> extract(ContentType contentType, String content) {
        ContentExtractor extractor = findExtractor(contentType);
        try {
            return extractor.extract(content);
        } catch (AppException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AppException(ErrorType.EXTRACTION_FAILED, e, contentType, "unexpected error");
        }
    }
}
