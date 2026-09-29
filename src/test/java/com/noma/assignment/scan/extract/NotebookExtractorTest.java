package com.noma.assignment.scan.extract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.noma.assignment.common.exceptions.AppException;
import com.noma.assignment.common.exceptions.ErrorType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class NotebookExtractorTest {

    private final NotebookExtractor extractor = new NotebookExtractor(JsonMapper.builder().build());

    @Test
    void extractsSourcesAsStringOrLineArray_andIgnoresOutputs() {
        String notebook = """
                {"cells": [
                  {"cell_type": "code", "source": ["import os\\n", "import sys\\n"],
                   "outputs": [{"output_type": "stream", "text": ["import from_output\\n"]}]},
                  {"cell_type": "markdown", "source": "# title"},
                  {"cell_type": "code", "source": "from x import y", "outputs": []}
                ]}
                """;

        assertThat(extractor.extract(notebook))
                .containsExactly("import os\nimport sys\n", "# title", "from x import y");
    }

    @Test
    void emptyCells_returnsNoChunks() {
        assertThat(extractor.extract("{\"cells\": []}")).isEmpty();
    }

    @Test
    void missingCells_throwsInvalidNotebook() {
        assertThatThrownBy(() -> extractor.extract("{\"metadata\": {}}"))
                .isInstanceOf(AppException.class)
                .extracting("type").isEqualTo(ErrorType.INVALID_NOTEBOOK);
    }

    @Test
    void malformedJson_throwsInvalidNotebook() {
        assertThatThrownBy(() -> extractor.extract("import os"))
                .isInstanceOf(AppException.class)
                .extracting("type").isEqualTo(ErrorType.INVALID_NOTEBOOK);
    }
}
