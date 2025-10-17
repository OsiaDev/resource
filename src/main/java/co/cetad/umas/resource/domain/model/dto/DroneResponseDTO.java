package co.cetad.umas.resource.domain.model.dto;

import co.cetad.umas.resource.domain.model.vo.DroneStatus;

public record DroneResponseDTO(
        String id,
        String vehicleId,
        String model,
        String description,
        String serialNumber,
        DroneStatus status,
        String createdAt,
        String updatedAt
) {
}
