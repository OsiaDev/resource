package co.cetad.umas.resource.domain.model.entity;

import co.cetad.umas.resource.domain.model.vo.OperatorStatus;

import java.time.LocalDateTime;

public record OperatorEntity(
        String id,
        String username,
        String fullName,
        String email,
        String phoneNumber,
        String ugcsUserId,
        String keycloakUserId,
        OperatorStatus status,
        Boolean isAvailable,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}