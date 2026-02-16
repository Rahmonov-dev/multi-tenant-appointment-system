package org.architect.multitenantappointmentsystem.dto.request;

import jakarta.validation.constraints.Size;
import org.architect.multitenantappointmentsystem.entity.StaffRole;

public record UpdateStaffRequest(
        StaffRole role,

        @Size(max = 100, message = "Display name 100 ta belgidan oshmasligi kerak")
        String displayName,

        @Size(max = 100, message = "Position 100 ta belgidan oshmasligi kerak")
        String position,

        Boolean isActive
) {}