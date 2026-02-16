package org.architect.multitenantappointmentsystem.dto.response;

import org.architect.multitenantappointmentsystem.entity.StaffRole;
import org.architect.multitenantappointmentsystem.entity.Staff;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public record StaffDetailResponse(
        Long id,
        String displayName,
        String position,
        StaffRole role,
        Boolean isActive,

        // user info
        Long userId,
        String fullName,
        String email,
        String phone,

        // services
        List<ServiceShortResponse> services,

        // schedules
        List<StaffScheduleResponse> schedules,

        // statistics
        Long totalAppointments,
        Long upcomingAppointments,

        LocalDateTime createdAt
) {
    public static StaffDetailResponse fromEntity(Staff staff) {
        if (staff == null) return null;

        var user = staff.getUser();

        // services mapping
        List<ServiceShortResponse> services = staff.getEmployements().stream()
                .map(employment -> new ServiceShortResponse(
                        employment.getId(),
                        employment.getName(),
                        employment.getDescription(),
                        employment.getDuration(),
                        employment.getIsActive()
                ))
                .collect(Collectors.toList());

        // schedules mapping
        List<StaffScheduleResponse> schedules = StaffScheduleResponse.fromEntityList(staff.getSchedules());

        long totalAppointments = staff.getAppointments() != null ? staff.getAppointments().size() : 0;
        long upcomingAppointments = staff.getAppointments().stream()
                .filter(app -> app.getStartTime().isAfter(LocalTime.from(LocalDateTime.now())))
                .count();

        return new StaffDetailResponse(
                staff.getId(),
                staff.getDisplayName(),
                staff.getPosition(),
                staff.getRole(),
                staff.getIsActive(),

                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                user.getPhone(),

                services,
                schedules,
                totalAppointments,
                upcomingAppointments,
                staff.getCreatedAt()
        );
    }}
