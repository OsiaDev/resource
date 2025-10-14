package co.cetad.umas.resource.domain.model.entity;

import co.cetad.umas.resource.domain.model.vo.DroneStatus;

import java.time.LocalDateTime;

public record DroneEntity(
        String id,
        String vehicleId,
        String model,
        String description,
        String serialNumber,
        DroneStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
