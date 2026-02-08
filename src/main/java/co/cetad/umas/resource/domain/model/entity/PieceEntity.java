package co.cetad.umas.resource.domain.model.entity;

import java.time.LocalDateTime;

public record PieceEntity(
        String id,
        String name,
        String description,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}