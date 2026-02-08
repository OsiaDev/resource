package co.cetad.umas.resource.domain.model.entity;

import co.cetad.umas.resource.domain.model.vo.MaintenanceStatus;

import java.time.LocalDateTime;

/**
 * Entidad que representa el historial de cambios de estado de un mantenimiento.
 * Permite auditoría y seguimiento de quién cambió el estado y cuándo.
 */
public record MaintenanceStatusHistoryEntity(
        String id,
        String maintenanceId,
        MaintenanceStatus status,
        LocalDateTime changedAt,
        String changedBy,
        String comment
) {
}