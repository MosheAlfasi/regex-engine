package com.noma.assignment.scan.api;

import com.noma.assignment.scan.api.dto.ScanRequest;
import com.noma.assignment.scan.api.dto.ScanResponse;
import com.noma.assignment.scan.service.ScanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;

    @PostMapping
    public ScanResponse scan(@Valid @RequestBody ScanRequest request) {
        return scanService.scan(request);
    }
}
