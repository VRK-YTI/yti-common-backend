package fi.vm.yti.common.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.yti.security.AuthorizationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.*;

public class ExceptionHandlerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Throwable.class)
    public void logAll(Throwable throwable,
                       HttpServletRequest request) throws Throwable {
        logger.error("Rogue catchable thrown while handling request to \"" + request.getServletPath() + "\"", throwable);
        throw throwable;
    }

    @Override
    protected @NotNull ResponseEntity<Object> handleHttpMessageNotReadable(
            @NotNull HttpMessageNotReadableException ex,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatusCode status,
            @NotNull WebRequest request) {
        return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex));
    }

    @ExceptionHandler(AuthorizationException.class)
    protected ResponseEntity<Object> handleAuthorizationException(AuthorizationException ex) {
        var apiError = new ApiError(HttpStatus.UNAUTHORIZED);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException ex) {
        var apiError = new ApiValidationError(BAD_REQUEST);
        apiError.setMessage("Object validation failed");
        var errors = ex.getConstraintViolations().stream()
                .map(c -> new ApiValidationErrorDetails(
                            c.getMessage(),
                            ((PathImpl) c.getPropertyPath()).getLeafNode().getName(),
                            c.getInvalidValue().toString())
                )
                .toList();
        try {
            logger.error("Constraint validation exception: " + ex.getMessage());
            logger.error(new ObjectMapper().writeValueAsString(errors));
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        apiError.setDetails(errors);
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(MappingError.class)
    protected  ResponseEntity<Object> handleMappingError(MappingError error){
        var apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage(error.getMessage());
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleNotFoundException(ResourceNotFoundException ex) {
        var apiError = new ApiError(NOT_FOUND);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(ResourceExistsException.class)
    protected ResponseEntity<Object> handleResourceExistsException(ResourceExistsException ex) {
        var apiError = new ApiError(CONFLICT);
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    public ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
