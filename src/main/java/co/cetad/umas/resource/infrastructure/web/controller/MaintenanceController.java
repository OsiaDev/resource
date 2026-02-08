package co.cetad.umas.resource.infrastructure.web.controller;

import co.cetad.umas.resource.application.service.maintenance.MaintenanceService;
import co.cetad.umas.resource.domain.model.dto.MaintenanceCreateRequestDTO;
import co.cetad.umas.resource.domain.model.dto.MaintenanceResponseDTO;
import co.cetad.umas.resource.domain.model.dto.MaintenanceUpdateRequestDTO;
import co.cetad.umas.resource.domain.model.entity.MaintenanceEntity;
import co.cetad.umas.resource.domain.model.vo.MaintenanceStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenances")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    /**
     * Obtiene todos los mantenimientos
     * GET /api/v1/maintenances
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<MaintenanceResponseDTO> getAllMaintenances() {
        return maintenanceService.getAllMaintenances()
                .map(this::toResponse);
    }

    /**
     * Obtiene un mantenimiento por su ID
     * GET /api/v1/maintenances/{id}
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MaintenanceResponseDTO>> getMaintenanceById(@PathVariable String id) {
        return Mono.fromFuture(maintenanceService.getMaintenanceById(id))
                .map(maintenance -> ResponseEntity.ok(toResponse(maintenance)))
                .onErrorResume(MaintenanceService.MaintenanceNotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Obtiene todos los mantenimientos de un drone específico
     * GET /api/v1/maintenances/drone/{droneId}
     */
    @GetMapping(value = "/drone/{droneId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<MaintenanceResponseDTO> getMaintenancesByDroneId(@PathVariable String droneId) {
        return maintenanceService.getMaintenancesByDroneId(droneId)
                .map(this::toResponse);
    }

    /**
     * Obtiene todos los mantenimientos por estado
     * GET /api/v1/maintenances/status/{status}
     */
    @GetMapping(value = "/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<MaintenanceResponseDTO> getMaintenancesByStatus(@PathVariable MaintenanceStatus status) {
        return maintenanceService.getMaintenancesByStatus(status)
                .map(this::toResponse);
    }

    /**
     * Crea un nuevo mantenimiento
     * POST /api/v1/maintenances
     * Automáticamente actualiza el estado del drone a IN_MAINTENANCE
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MaintenanceResponseDTO>> createMaintenance(
            @RequestBody MaintenanceCreateRequestDTO request) {
        return Mono.fromFuture(maintenanceService.createMaintenance(request))
                .map(maintenance -> ResponseEntity.status(HttpStatus.CREATED).body(toResponse(maintenance)))
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.just(ResponseEntity.badRequest().build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * Actualiza un mantenimiento existente
     * PUT /api/v1/maintenances/{id}
     * Si el estado cambia a COMPLETED, actualiza el drone a ACTIVE
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MaintenanceResponseDTO>> updateMaintenance(
            @PathVariable String id,
            @RequestBody MaintenanceUpdateRequestDTO request) {
        return Mono.fromFuture(maintenanceService.updateMaintenance(id, request))
                .map(maintenance -> ResponseEntity.ok(toResponse(maintenance)))
                .onErrorResume(MaintenanceService.MaintenanceNotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    /**
     * Verifica si un drone tiene mantenimiento activo
     * GET /api/v1/maintenances/drone/{droneId}/active
     */
    @GetMapping(value = "/drone/{droneId}/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ActiveMaintenanceResponse>> hasActiveMaintenance(@PathVariable String droneId) {
        return Mono.fromFuture(maintenanceService.hasActiveMaintenance(droneId))
                .map(hasActive -> ResponseEntity.ok(new ActiveMaintenanceResponse(droneId, hasActive)));
    }

    /**
     * Obtiene todos los estados posibles de mantenimiento
     * GET /api/v1/maintenances/statuses
     */
    @GetMapping(value = "/statuses", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<MaintenanceStatusResponse>> getAllStatuses() {
        return Mono.just(
                Arrays.stream(MaintenanceStatus.values())
                        .map(status -> new MaintenanceStatusResponse(
                                status.name(),
                                status.getStatus()
                        ))
                        .toList()
        );
    }

    private MaintenanceResponseDTO toResponse(MaintenanceEntity entity) {
        return new MaintenanceResponseDTO(
                entity.id(),
                entity.droneId(),
                entity.currentStatus(),
                entity.description(),
                entity.createdAt().toString(),
                entity.updatedAt().toString()
        );
    }

    private record ActiveMaintenanceResponse(String droneId, boolean hasActiveMaintenance) {}
    private record MaintenanceStatusResponse(String code, String description) {}

}