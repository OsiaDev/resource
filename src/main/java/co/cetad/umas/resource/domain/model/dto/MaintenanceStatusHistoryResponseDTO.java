package co.cetad.umas.resource.domain.model.dto;

import co.cetad.umas.resource.domain.model.vo.MaintenanceStatus;

public record MaintenanceStatusHistoryResponseDTO(
        String id,
        String maintenanceId,
        MaintenanceStatus status,
        String changedAt,
        String changedBy,
        String comment
) {
}