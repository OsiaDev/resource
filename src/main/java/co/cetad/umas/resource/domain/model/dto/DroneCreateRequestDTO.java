package co.cetad.umas.resource.domain.model.dto;

import java.math.BigDecimal;

public record DroneCreateRequestDTO(
        String name,
        String vehicleId,
        String model,
        String description,
        String serialNumber,
        BigDecimal flightHours
) {
}