package co.cetad.umas.resource.domain.model.dto;

import co.cetad.umas.resource.domain.model.vo.DroneStatus;

import java.math.BigDecimal;

public record DroneUpdateRequestDTO(
        String vehicleId,
        String model,
        String description,
        String serialNumber,
        DroneStatus status,
        BigDecimal flightHours
) {
}