package com.noma.assignment.common.advice;

import java.util.stream.Collectors;

import com.noma.assignment.common.exceptions.AppException;
import com.noma.assignment.common.exceptions.ErrDTO;
import com.noma.assignment.common.exceptions.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // AppException is thrown by the service layer
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrDTO> handleAppException(AppException e) {
        log.warn("App exception [{}]: {}", e.getType(), e.getMessage(), e);
        return respond(new ErrDTO(e.getType(), e.getMessage()));
    }

    // Jackson failed to convert JSON → ScanRequest
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrDTO> handleValidation(MethodArgumentNotValidException e) {
        String details = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", details);
        return respond(ErrDTO.of(ErrorType.VALIDATION_FAILED, details));
    }

    // @Valid checks the annotations on the object
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrDTO> handleUnreadable(HttpMessageNotReadableException e) {
        log.warn("Malformed request body: {}", e.getMessage());
        return respond(ErrDTO.of(ErrorType.MALFORMED_REQUEST));
    }

    // General exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrDTO> handleUnexpected(Exception e) {
        log.error("Unexpected exception", e);
        return respond(ErrDTO.of(ErrorType.INTERNAL_ERROR));
    }

    private static ResponseEntity<ErrDTO> respond(ErrDTO body) {
        return ResponseEntity.status(body.type().getHttpStatus()).body(body);
    }
}
