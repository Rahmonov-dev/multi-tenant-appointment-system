package org.architect.multitenantappointmentsystem.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

class ResponseDtoTest {
    @Test
    @DisplayName("ok() method should return succesful response with data")
    void ok_shouldReturnResponseWithData() {
        String testData = "Test Data";
        ResponseDto<String> response = ResponseDto.ok(testData);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(testData);
        assertThat(response.getMessage()).isNull();
    }

    @Test
    @DisplayName("ok(data, message) should return response with both data and message")
    void ok_WithMessage_ShouldReturnCorrectResponse() {
        String data = "Test Data";
        String message = "Success Message";

        ResponseDto<?> response = ResponseDto.ok(data, message);
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("unauthorized() should return 401 status code")
    void unauthorized_ShouldReturn401StatusCode() {
        ResponseDto<?> response= ResponseDto.unauthorized();

        assertThat(response.getStatusCode()).isEqualTo(401);
        assertThat(response.getSuccess()).isNull();
    }
}