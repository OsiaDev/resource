package co.cetad.umas.resource.domain.model.dto;

public record MaintenanceCreateRequestDTO(
        String droneId,
        String description
) {
}