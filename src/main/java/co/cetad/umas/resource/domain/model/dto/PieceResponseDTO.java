package co.cetad.umas.resource.domain.model.dto;

public record PieceResponseDTO(
        String id,
        String name,
        String description,
        Boolean active,
        String createdAt,
        String updatedAt
) {
}