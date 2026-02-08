package co.cetad.umas.resource.domain.model.dto;

import co.cetad.umas.resource.domain.model.vo.MaintenanceStatus;

public record MaintenanceStatusUpdateDTO(
        MaintenanceStatus status,
        String changedBy,
        String comment
) {
}