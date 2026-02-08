package co.cetad.umas.resource.domain.model.entity;

import co.cetad.umas.resource.domain.model.vo.MaintenancePieceStatus;

import java.time.LocalDateTime;

/**
 * Entidad que representa la relación entre un mantenimiento y una pieza específica.
 * Permite hacer seguimiento del estado de cada pieza durante el mantenimiento.
 */
public record MaintenancePieceEntity(
        String id,
        String maintenanceId,
        String pieceId,
        MaintenancePieceStatus status,
        Integer quantity,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}