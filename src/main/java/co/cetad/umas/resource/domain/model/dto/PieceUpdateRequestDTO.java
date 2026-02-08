package co.cetad.umas.resource.domain.model.dto;

public record PieceUpdateRequestDTO(
        String name,
        String description,
        Boolean active
) {
}