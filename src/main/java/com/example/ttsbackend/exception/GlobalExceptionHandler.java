package com.example.ttsbackend.exception;

import com.example.ttsbackend.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** @Valid failures on request body — empty text, invalid pattern, etc. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("Validation error: {}", message);
        ErrorResponse error = new ErrorResponse(
                false, HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR", message);
        return ResponseEntity.badRequest().body(error);
    }

    /** Malformed JSON in request body */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex) {

        log.warn("Malformed JSON: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                false,
                HttpStatus.BAD_REQUEST.value(),
                "MALFORMED_JSON",
                "Request body is not valid JSON.");
        return ResponseEntity.badRequest().body(error);
    }

    /** Wrong Content-Type (e.g. text/plain instead of application/json) */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMedia(
            HttpMediaTypeNotSupportedException ex) {

        ErrorResponse error = new ErrorResponse(
                false,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "UNSUPPORTED_MEDIA_TYPE",
                "Content-Type must be application/json.");
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error);
    }

    /** Missing required query param */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex) {

        ErrorResponse error = new ErrorResponse(
                false,
                HttpStatus.BAD_REQUEST.value(),
                "MISSING_PARAMETER",
                "Missing required parameter: " + ex.getParameterName());
        return ResponseEntity.badRequest().body(error);
    }

    /** External service unreachable (network failure) */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleNetwork(ResourceAccessException ex) {
        log.error("Network error calling external service: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                false,
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "NETWORK_ERROR",
                "Cannot reach the TTS service. Please check your internet connection.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /** Our own TTS exceptions */
    @ExceptionHandler(TtsException.class)
    public ResponseEntity<ErrorResponse> handleTts(TtsException ex) {
        log.warn("TtsException: {} (status={})", ex.getMessage(), ex.getStatusCode());
        ErrorResponse error = new ErrorResponse(
                false, ex.getStatusCode(), "TTS_ERROR", ex.getMessage());
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    /** Authentication errors (bad password, user not found) */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(Exception ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                false,
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_CREDENTIALS",
                "Invalid email or password.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthError(AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                false,
                HttpStatus.UNAUTHORIZED.value(),
                "AUTHENTICATION_ERROR",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /** Catch-all for anything else */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        ErrorResponse error = new ErrorResponse(
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.");
        return ResponseEntity.internalServerError().body(error);
    }
}