package org.architect.multitenantappointmentsystem.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceStatisticsResponse(
        UUID tenantId,
        Long totalServices,
        Long activeServices,
        Long inactiveServices,
        BigDecimal averagePrice,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer averageDuration,
        Integer minDuration,
        Integer maxDuration,
        Long totalAppointments
) {}
