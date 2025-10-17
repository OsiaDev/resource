package co.cetad.umas.resource.infrastructure.web;

import co.cetad.umas.resource.application.service.drone.DroneService;
import co.cetad.umas.resource.domain.model.dto.DroneResponseDTO;
import co.cetad.umas.resource.domain.model.entity.DroneEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/drones")
public class DroneController {

    private final DroneService droneService;

    public DroneController(DroneService droneService) {
        this.droneService = droneService;
    }

    /**
     * Obtiene todos los drones
     * GET /api/v1/drones
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<DroneResponseDTO> getAllDrones() {
        return droneService
                .getAllDrones()
                .map(this::toResponse);
    }

    private DroneResponseDTO toResponse(DroneEntity droneEntity) {
        return new DroneResponseDTO(
                droneEntity.id(),
                droneEntity.vehicleId(),
                droneEntity.model(),
                droneEntity.description(),
                droneEntity.serialNumber(),
                droneEntity.status(),
                droneEntity.createdAt().toString(),
                droneEntity.updatedAt().toString()
        );
    }

}
