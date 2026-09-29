package com.noma.assignment.scan.match;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.noma.assignment.common.exceptions.AppException;
import com.noma.assignment.common.exceptions.ErrorType;
import com.noma.assignment.config.ScanProperties;
import com.noma.assignment.scan.api.dto.ScanResponse.PatternMatches;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegexMatcher {

    private final ScanProperties props;

    /** Patterns are compiled up front so an invalid one fails the request before any content is fetched. */
    public List<Pattern> compile(List<String> patterns) {
        if (patterns.size() > props.maxPatterns()) {
            throw new AppException(ErrorType.INVALID_ARGUMENT, "at most " + props.maxPatterns() + " patterns are allowed");
        }
        return patterns.stream().map(this::compile).toList();
    }

    /** Returns, per pattern, every matched text across all chunks, in order of appearance. */
    public List<PatternMatches> match(List<Pattern> patterns, List<String> chunks) {
        return patterns.stream()
                .map(pattern -> new PatternMatches(pattern.pattern(), findAll(pattern, chunks)))
                .toList();
    }

    private Pattern compile(String regex) {
        if (regex.length() > props.maxPatternLength()) {
            throw new AppException(ErrorType.INVALID_PATTERN, "longer than " + props.maxPatternLength() + " characters");
        }
        try {
            return Pattern.compile(regex, Pattern.MULTILINE);
        } catch (PatternSyntaxException e) {
            throw new AppException(ErrorType.INVALID_PATTERN, e, e.getDescription() + " in '" + regex + "'");
        }
    }

    private static List<String> findAll(Pattern pattern, List<String> chunks) {
        List<String> matches = new ArrayList<>();
        for (String chunk : chunks) {
            Matcher matcher = pattern.matcher(chunk);
            while (matcher.find()) {
                matches.add(matcher.group());
            }
        }
        return matches;
    }
}
