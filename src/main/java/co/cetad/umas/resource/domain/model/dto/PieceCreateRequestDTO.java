package co.cetad.umas.resource.domain.model.dto;

public record PieceCreateRequestDTO(
        String name,
        String description,
        Boolean active
) {
}