package co.cetad.umas.resource.domain.model.dto;

public record DroneCreateRequestDTO(
        String vehicleId,
        String model,
        String description,
        String serialNumber
) {
}