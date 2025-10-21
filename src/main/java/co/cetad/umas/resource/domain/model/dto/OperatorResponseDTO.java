package co.cetad.umas.resource.domain.model.dto;

import co.cetad.umas.resource.domain.model.vo.OperatorStatus;

public record OperatorResponseDTO(
        String id,
        String username,
        String fullName,
        String email,
        String phoneNumber,
        String ugcsUserId,
        OperatorStatus status,
        Boolean isAvailable,
        String createdAt,
        String updatedAt
) {
}