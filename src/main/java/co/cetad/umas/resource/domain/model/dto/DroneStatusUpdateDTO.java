package co.cetad.umas.resource.domain.model.dto;

import co.cetad.umas.resource.domain.model.vo.DroneStatus;

public record DroneStatusUpdateDTO(
        DroneStatus status
) {
}