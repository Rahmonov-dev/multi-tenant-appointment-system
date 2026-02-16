package org.architect.multitenantappointmentsystem.dto.response;

import org.architect.multitenantappointmentsystem.entity.StaffSchedule;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public record StaffScheduleResponse(
        Long id,
        Long staffId,
        String staffName,
        Integer dayOfWeek,
        String dayName,
        String startTime,
        String endTime,
        Boolean isAvailable
) {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static StaffScheduleResponse fromEntity(StaffSchedule schedule) {
        return new StaffScheduleResponse(
                schedule.getId(),
                schedule.getStaff().getId(),
                schedule.getStaff().getDisplayName(),
                schedule.getDayOfWeek(),
                schedule.getDayName(),
                schedule.getStartTime().format(TIME_FORMATTER),
                schedule.getEndTime().format(TIME_FORMATTER),
                schedule.getIsAvailable()
        );
    }
    public static List<StaffScheduleResponse> fromEntityList(List<StaffSchedule> schedules) {
        return schedules.stream()
                .map(StaffScheduleResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
