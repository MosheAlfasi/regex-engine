package com.noma.assignment.scan.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.List;

import com.noma.assignment.TestProps;
import com.noma.assignment.common.exceptions.AppException;
import com.noma.assignment.common.exceptions.ErrorType;
import com.noma.assignment.scan.api.dto.ScanResponse.PatternMatches;
import org.junit.jupiter.api.Test;

class RegexMatcherTest {

    private final RegexMatcher matcher = new RegexMatcher(TestProps.withBaseDir(Path.of(".")));

    @Test
    void returnsAllMatchesPerPattern_acrossChunks_withMultilineAnchors() {
        List<PatternMatches> result = matcher.match(
                matcher.compile(List.of("^import \\w+", "<a href=\"[^\"]+\"")),
                List.of("import os\nx = 1\nimport sys", "<a href=\"http://a\"> <a href=\"http://b\">"));

        assertThat(result).containsExactly(
                new PatternMatches("^import \\w+", List.of("import os", "import sys")),
                new PatternMatches("<a href=\"[^\"]+\"", List.of("<a href=\"http://a\"", "<a href=\"http://b\"")));
    }

    @Test
    void noMatches_returnsEmptyList() {
        assertThat(matcher.match(matcher.compile(List.of("zzz")), List.of("abc")))
                .containsExactly(new PatternMatches("zzz", List.of()));
    }

    @Test
    void invalidPattern_throwsInvalidPattern() {
        assertThatThrownBy(() -> matcher.compile(List.of("(")))
                .isInstanceOf(AppException.class)
                .extracting("type").isEqualTo(ErrorType.INVALID_PATTERN);
    }

    @Test
    void tooManyPatterns_rejected() {
        assertThatThrownBy(() -> matcher.compile(List.of("a", "b", "c", "d")))
                .isInstanceOf(AppException.class)
                .extracting("type").isEqualTo(ErrorType.INVALID_ARGUMENT);
    }

    @Test
    void tooLongPattern_rejected() {
        assertThatThrownBy(() -> matcher.compile(List.of("a".repeat(51))))
                .isInstanceOf(AppException.class)
                .extracting("type").isEqualTo(ErrorType.INVALID_PATTERN);
    }
}
