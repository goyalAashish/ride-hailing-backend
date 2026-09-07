package com.ridehailing.exception;

import com.ridehailing.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private MissingRequestHeaderException missingRequestHeaderException;

    @Test
    void resourceNotFoundException_mapsToHttp404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("User", 42L);

        ResponseEntity<ApiResponse<Void>> response = handler.handleRideHailingException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getStatusCode()).isEqualTo(404);
        assertThat(response.getBody().getError().getCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getBody().getMessage()).contains("User").contains("42");
    }

    @Test
    void duplicateResourceException_mapsToHttp400() {
        DuplicateResourceException ex = new DuplicateResourceException("Driver", "phone", "9999999999");

        ResponseEntity<ApiResponse<Void>> response = handler.handleRideHailingException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError().getCode()).isEqualTo("DUPLICATE_RESOURCE");
    }

    @Test
    void genericBadRequestException_mapsToHttp400WithCustomCode() {
        BadRequestException ex = new BadRequestException("NO_DRIVER_AVAILABLE", "No drivers available nearby");

        ResponseEntity<ApiResponse<Void>> response = handler.handleRideHailingException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError().getCode()).isEqualTo("NO_DRIVER_AVAILABLE");
        assertThat(response.getBody().getMessage()).isEqualTo("No drivers available nearby");
    }

    @Test
    void validationException_collectsAllFieldErrorsInto400() {
        FieldError fieldError1 = new FieldError("request", "phone", "must not be blank");
        FieldError fieldError2 = new FieldError("request", "name", "must not be blank");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidationException(methodArgumentNotValidException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getError().getDetails())
                .containsExactlyInAnyOrder("phone: must not be blank", "name: must not be blank");
    }

    @Test
    void missingRequestHeader_mapsToHttp400() {
        when(missingRequestHeaderException.getHeaderName()).thenReturn("X-User-Id");

        ResponseEntity<ApiResponse<Void>> response = handler.handleMissingHeader(missingRequestHeaderException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError().getCode()).isEqualTo("MISSING_HEADER");
        assertThat(response.getBody().getError().getDetails().get(0)).contains("X-User-Id");
    }

    @Test
    void unexpectedException_mapsToHttp500() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpected(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError().getCode()).isEqualTo("INTERNAL_ERROR");
    }
}
