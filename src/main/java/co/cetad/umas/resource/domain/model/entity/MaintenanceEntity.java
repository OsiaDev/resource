package co.cetad.umas.resource.domain.model.entity;

import co.cetad.umas.resource.domain.model.vo.MaintenanceStatus;

import java.time.LocalDateTime;

public record MaintenanceEntity(
        String id,
        String droneId,
        MaintenanceStatus currentStatus,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}