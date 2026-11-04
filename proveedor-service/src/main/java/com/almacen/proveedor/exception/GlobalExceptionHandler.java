package com.almacen.proveedor.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest req) { return error(HttpStatus.NOT_FOUND, ex.getMessage(), req, List.of()); }
    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> conflict(ConflictException ex, HttpServletRequest req) { return error(HttpStatus.CONFLICT, ex.getMessage(), req, List.of()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var fields = ex.getBindingResult().getFieldErrors().stream().map(e -> new ApiError.FieldError(e.getField(), e.getDefaultMessage())).toList();
        return error(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos", req, fields);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> invalidQuery(ConstraintViolationException ex, HttpServletRequest req) { return error(HttpStatus.BAD_REQUEST, ex.getMessage(), req, List.of()); }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> malformed(Exception ex, HttpServletRequest req) { return error(HttpStatus.BAD_REQUEST, "La solicitud contiene un valor inválido", req, List.of()); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> duplicate(DataIntegrityViolationException ex, HttpServletRequest req) { return error(HttpStatus.CONFLICT, "El documento ya está registrado", req, List.of()); }
    private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest req, List<ApiError.FieldError> fields) {
        return ResponseEntity.status(status).body(new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, req.getRequestURI(), fields));
    }
}
