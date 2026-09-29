package com.noma.assignment.scan.extract;

import java.util.List;

import org.springframework.stereotype.Component;

/** Scans the content as-is: HTML pages, source code, any plain text. */
@Component
public class RawTextExtractor implements ContentExtractor {

    @Override
    public boolean supports(ContentType contentType) {
        return contentType == ContentType.RAW;
    }

    @Override
    public List<String> extract(String content) {
        return List.of(content);
    }
}
