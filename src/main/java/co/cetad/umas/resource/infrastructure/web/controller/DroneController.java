package co.cetad.umas.resource.infrastructure.web.controller;

import co.cetad.umas.resource.application.service.drone.DroneService;
import co.cetad.umas.resource.domain.model.dto.DroneCreateRequestDTO;
import co.cetad.umas.resource.domain.model.dto.DroneResponseDTO;
import co.cetad.umas.resource.domain.model.dto.DroneStatusUpdateDTO;
import co.cetad.umas.resource.domain.model.dto.DroneUpdateRequestDTO;
import co.cetad.umas.resource.domain.model.entity.DroneEntity;
import co.cetad.umas.resource.domain.model.vo.DroneStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    /**
     * Obtiene todos los drones activos
     * GET /api/v1/drones/active
     */
    @GetMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<DroneResponseDTO> getActiveDrones() {
        return droneService
                .getActiveDrones()
                .map(this::toResponse);
    }

    /**
     * Obtiene un drone por ID
     * GET /api/v1/drones/{id}
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<DroneResponseDTO> getDroneById(@PathVariable String id) {
        return Mono.fromFuture(
                droneService.getDroneById(id)
                        .thenApply(this::toResponse)
        );
    }

    /**
     * Crea un nuevo drone
     * POST /api/v1/drones
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DroneResponseDTO> createDrone(@RequestBody DroneCreateRequestDTO request) {
        return Mono.fromFuture(
                droneService.createDrone(request)
                        .thenApply(this::toResponse)
        );
    }

    /**
     * Actualiza un drone existente
     * PUT /api/v1/drones/{id}
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<DroneResponseDTO> updateDrone(
            @PathVariable String id,
            @RequestBody DroneUpdateRequestDTO request) {
        return Mono.fromFuture(
                droneService.updateDrone(id, request)
                        .thenApply(this::toResponse)
        );
    }

    /**
     * Actualiza solo el estado de un drone
     * PATCH /api/v1/drones/{id}/status
     */
    @PatchMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<DroneResponseDTO> updateDroneStatus(
            @PathVariable String id,
            @RequestBody DroneStatusUpdateDTO request) {
        return Mono.fromFuture(
                droneService.updateDroneStatus(id, request.status())
                        .thenApply(this::toResponse)
        );
    }

    /**
     * Elimina un drone (soft delete cambiando estado a DECOMMISSIONED)
     * DELETE /api/v1/drones/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteDrone(@PathVariable String id) {
        return Mono.fromFuture(
                droneService.deleteDrone(id)
                        .thenApply(v -> null)
        );
    }

    /**
     * Obtiene todos los estados posibles de un drone
     * GET /api/v1/drones/statuses
     */
    @GetMapping(value = "/statuses", produces = MediaType.APPLICATION_JSON_VALUE)
    public CompletableFuture<List<DroneStatusResponse>> getAllStatuses() {
        return CompletableFuture.supplyAsync(() ->
                Arrays.stream(DroneStatus.values())
                        .map(status -> new DroneStatusResponse(
                                status.name(),
                                status.getStatus()
                        ))
                        .toList()
        );
    }

    private DroneResponseDTO toResponse(DroneEntity droneEntity) {
        return new DroneResponseDTO(
                droneEntity.id(),
                droneEntity.name(),
                droneEntity.vehicleId(),
                droneEntity.model(),
                droneEntity.description(),
                droneEntity.serialNumber(),
                droneEntity.status(),
                droneEntity.flightHours(),
                droneEntity.createdAt().toString(),
                droneEntity.updatedAt().toString()
        );
    }

    private record DroneStatusResponse(String code, String description) {}

}