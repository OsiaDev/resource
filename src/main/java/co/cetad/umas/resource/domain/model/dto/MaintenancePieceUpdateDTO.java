package co.cetad.umas.resource.domain.model.dto;

import co.cetad.umas.resource.domain.model.vo.MaintenancePieceStatus;

public record MaintenancePieceUpdateDTO(
        MaintenancePieceStatus status,
        String notes
) {
}