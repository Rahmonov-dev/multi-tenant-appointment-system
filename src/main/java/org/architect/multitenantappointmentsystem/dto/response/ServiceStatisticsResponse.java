package org.architect.multitenantappointmentsystem.dto.response;

import java.math.BigDecimal;

public record ServiceStatisticsResponse(
        Long tenantId,
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
