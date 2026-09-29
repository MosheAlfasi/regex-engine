package com.noma.assignment.scan.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.noma.assignment.common.exceptions.AppException;
import com.noma.assignment.common.exceptions.ErrorType;
import com.noma.assignment.scan.api.dto.ScanResponse;
import com.noma.assignment.scan.api.dto.ScanResponse.PatternMatches;
import com.noma.assignment.scan.service.ScanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ScanController.class)
class ScanControllerTest {

    private static final String VALID_BODY = """
            {"source": {"type": "HTTP", "url": "https://example.com", "headers": {"Authorization": "Bearer t"}},
             "contentType": "RAW", "patterns": ["<a href"]}
            """;

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ScanService scanService;

    @Test
    void scan_returnsMatches() throws Exception {
        when(scanService.scan(any())).thenReturn(
                new ScanResponse(List.of(new PatternMatches("<a href", List.of("<a href")))));

        mvc.perform(post("/api/scan").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results[0].pattern").value("<a href"))
                .andExpect(jsonPath("$.results[0].matches[0]").value("<a href"));
    }

    @Test
    void invalidBody_returns400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/scan").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"source\": {\"type\": \"FILE\"}, \"contentType\": \"RAW\", \"patterns\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("VALIDATION_FAILED"));
    }

    @Test
    void unknownEnum_returns400() throws Exception {
        mvc.perform(post("/api/scan").contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("\"HTTP\"", "\"FTP\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("MALFORMED_REQUEST"));
    }

    @Test
    void appException_isMappedToItsStatus() throws Exception {
        when(scanService.scan(any())).thenThrow(new AppException(ErrorType.FETCH_FAILED, "https://example.com"));

        mvc.perform(post("/api/scan").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.type").value("FETCH_FAILED"));
    }
}
