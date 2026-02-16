package org.architect.multitenantappointmentsystem.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiError {
    private String path;
    private int status;
    private String code;
    private String message;
    private LocalDateTime timestamp;
}
