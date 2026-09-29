package com.noma.assignment.scan.extract;

import java.util.List;

/** Selects the parts of the content that should be scanned. */
public interface ContentExtractor {

    boolean supports(ContentType contentType);

    List<String> extract(String content);
}
