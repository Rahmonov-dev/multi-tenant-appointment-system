package org.architect.multitenantappointmentsystem.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdateServiceRequest(
        @Size(max = 200, message = "Employement nomi 200 ta belgidan oshmasligi kerak")
        String name,

        @Size(max = 5000, message = "Tavsif 5000 ta belgidan oshmasligi kerak")
        String description,

        @Min(value = 5, message = "Davomiylik kamida 5 minut bo'lishi kerak")
        @Max(value = 480, message = "Davomiylik maksimum 480 minut (8 soat) bo'lishi kerak")
        Integer duration,

        @DecimalMin(value = "0.0", inclusive = false, message = "Narx 0 dan katta bo'lishi kerak")
        @DecimalMax(value = "999999999.99", message = "Narx juda katta")
        BigDecimal price,

        @Size(max = 500, message = "Rasm URL 500 ta belgidan oshmasligi kerak")
        String imageUrl,

        Boolean isActive,

        @Min(value = 0, message = "Display order manfiy bo'lmasligi kerak")
        Integer displayOrder
) {}
