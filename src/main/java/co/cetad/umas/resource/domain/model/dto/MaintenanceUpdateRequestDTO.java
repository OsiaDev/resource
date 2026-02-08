package co.cetad.umas.resource.domain.model.dto;

import co.cetad.umas.resource.domain.model.vo.MaintenanceStatus;

public record MaintenanceUpdateRequestDTO(
        MaintenanceStatus currentStatus,
        String description
) {
}