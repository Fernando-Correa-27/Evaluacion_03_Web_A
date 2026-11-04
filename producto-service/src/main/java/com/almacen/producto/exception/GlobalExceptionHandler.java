package com.almacen.producto.exception;

import feign.FeignException;
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
    @ExceptionHandler(ResourceNotFoundException.class) ResponseEntity<ApiError> notFound(ResourceNotFoundException e, HttpServletRequest r) { return error(HttpStatus.NOT_FOUND,e.getMessage(),r,List.of()); }
    @ExceptionHandler(ConflictException.class) ResponseEntity<ApiError> conflict(ConflictException e, HttpServletRequest r) { return error(HttpStatus.CONFLICT,e.getMessage(),r,List.of()); }
    @ExceptionHandler(UnprocessableException.class) ResponseEntity<ApiError> unprocessable(UnprocessableException e, HttpServletRequest r) { return error(HttpStatus.UNPROCESSABLE_ENTITY,e.getMessage(),r,List.of()); }
    @ExceptionHandler(ServiceUnavailableException.class) ResponseEntity<ApiError> unavailable(ServiceUnavailableException e,HttpServletRequest r) { return error(HttpStatus.SERVICE_UNAVAILABLE,e.getMessage(),r,List.of()); }
    @ExceptionHandler(FeignException.class) ResponseEntity<ApiError> remote(FeignException e, HttpServletRequest r) {
        if (e.status() == 404) return error(HttpStatus.NOT_FOUND,"El proveedor indicado no existe",r,List.of());
        return error(HttpStatus.SERVICE_UNAVAILABLE,"No fue posible consultar proveedor-service",r,List.of());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e, HttpServletRequest r) {
        var fields=e.getBindingResult().getFieldErrors().stream().map(x->new ApiError.FieldError(x.getField(),x.getDefaultMessage())).toList();
        return error(HttpStatus.BAD_REQUEST,"La solicitud contiene datos inválidos",r,fields);
    }
    @ExceptionHandler(ConstraintViolationException.class) ResponseEntity<ApiError> query(ConstraintViolationException e,HttpServletRequest r) { return error(HttpStatus.BAD_REQUEST,e.getMessage(),r,List.of()); }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class}) ResponseEntity<ApiError> malformed(Exception e,HttpServletRequest r) { return error(HttpStatus.BAD_REQUEST,"La solicitud contiene un valor inválido",r,List.of()); }
    @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ApiError> duplicate(DataIntegrityViolationException e,HttpServletRequest r) { return error(HttpStatus.CONFLICT,"El código de producto ya está registrado",r,List.of()); }
    private ResponseEntity<ApiError> error(HttpStatus s,String m,HttpServletRequest r,List<ApiError.FieldError> fields) { return ResponseEntity.status(s).body(new ApiError(LocalDateTime.now(),s.value(),s.getReasonPhrase(),m,r.getRequestURI(),fields)); }
}
