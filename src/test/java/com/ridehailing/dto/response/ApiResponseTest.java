package com.ridehailing.dto.response;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void success_defaultsToHttp200AndNullError() {
        ApiResponse<String> response = ApiResponse.success("Operation successful", "payload");

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Operation successful");
        assertThat(response.getData()).isEqualTo("payload");
        assertThat(response.getError()).isNull();
    }

    @Test
    void success_withExplicitStatus_usesThatStatus() {
        ApiResponse<Void> response = ApiResponse.success(HttpStatus.OK, "created", null);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    void failure_setsSuccessFalseAndNullData() {
        ErrorDetail error = ErrorDetail.of("NOT_FOUND", "User not found for identifier: 99");

        ApiResponse<Void> response = ApiResponse.failure(HttpStatus.NOT_FOUND, "User not found", error);

        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getError().getCode()).isEqualTo("NOT_FOUND");
        assertThat(response.getError().getDetails()).containsExactly("User not found for identifier: 99");
    }
}
