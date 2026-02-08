package co.cetad.umas.resource.domain.model.dto;

import co.cetad.umas.resource.domain.model.vo.MaintenanceStatus;

public record MaintenanceResponseDTO(
        String id,
        String droneId,
        MaintenanceStatus currentStatus,
        String description,
        String createdAt,
        String updatedAt
) {
}