package com.noma.assignment.scan.fetch;

import com.noma.assignment.scan.api.dto.SourceDTO;

/** Retrieves raw content from a source. */
public interface ContentFetcher {

    boolean supports(SourceDTO source);

    String fetch(SourceDTO source);
}
