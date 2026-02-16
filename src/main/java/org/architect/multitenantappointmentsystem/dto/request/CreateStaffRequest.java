package org.architect.multitenantappointmentsystem.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.architect.multitenantappointmentsystem.entity.StaffRole;

import java.util.List;

public record CreateStaffRequest(

        @NotNull(message = "User ID bo'sh bo'lmasligi kerak")
        Long userId,

        @NotNull(message = "Role tanlanishi kerak")
        StaffRole role,

        @NotBlank(message = "Display name bo'sh bo'lmasligi kerak")
        @Size(max = 100, message = "Display name 100 ta belgidan oshmasligi kerak")
        String displayName,

        @NotBlank(message = "Position bo'sh bo'lmasligi kerak")
        @Size(max = 100, message = "Position 100 ta belgidan oshmasligi kerak")
        String position,

        @Valid
        List<CreateStaffScheduleRequest> schedule
) {
}
