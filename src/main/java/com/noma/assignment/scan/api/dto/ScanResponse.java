package com.noma.assignment.scan.api.dto;

import java.util.List;

public record ScanResponse(List<PatternMatches> results) {

    public record PatternMatches(String pattern, List<String> matches) {
    }
}
