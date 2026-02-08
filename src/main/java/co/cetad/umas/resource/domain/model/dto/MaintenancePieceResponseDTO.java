package co.cetad.umas.resource.domain.model.dto;

import co.cetad.umas.resource.domain.model.vo.MaintenancePieceStatus;

public record MaintenancePieceResponseDTO(
        String id,
        String maintenanceId,
        String pieceId,
        String pieceName,
        String pieceCode,
        MaintenancePieceStatus status,
        Integer quantity,
        String notes,
        String createdAt,
        String updatedAt
) {
}