package co.cetad.umas.resource.infrastructure.web.controller;

import co.cetad.umas.resource.application.service.maintenance.MaintenanceService;
import co.cetad.umas.resource.domain.model.dto.MaintenanceCreateRequestDTO;
import co.cetad.umas.resource.domain.model.dto.MaintenanceResponseDTO;
import co.cetad.umas.resource.domain.model.dto.MaintenanceStatusHistoryResponseDTO;
import co.cetad.umas.resource.domain.model.dto.MaintenanceStatusUpdateDTO;
import co.cetad.umas.resource.domain.model.entity.MaintenanceEntity;
import co.cetad.umas.resource.domain.model.entity.MaintenanceStatusHistoryEntity;
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
     * Obtiene todos los mantenimientos de un drone específico (HISTORIAL DEL DRONE)
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
     * Automáticamente:
     * - Crea registros de todas las piezas activas en estado PENDING
     * - Cambia el estado del drone a IN_MAINTENANCE
     * - Crea registro inicial en historial
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
     * Actualiza el estado de un mantenimiento
     * PATCH /api/v1/maintenances/{id}/status
     * Si el estado cambia a COMPLETED, automáticamente cambia el drone a ACTIVE
     * Siempre crea un registro en el historial
     */
    @PatchMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<MaintenanceResponseDTO>> updateMaintenanceStatus(
            @PathVariable String id,
            @RequestBody MaintenanceStatusUpdateDTO statusUpdate) {
        return Mono.fromFuture(maintenanceService.updateMaintenanceStatus(id, statusUpdate))
                .map(maintenance -> ResponseEntity.ok(toResponse(maintenance)))
                .onErrorResume(MaintenanceService.MaintenanceNotFoundException.class,
                        e -> Mono.just(ResponseEntity.notFound().build()))
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.just(ResponseEntity.badRequest().build()));
    }

    /**
     * Obtiene el historial completo de estados de un mantenimiento
     * GET /api/v1/maintenances/{id}/history
     */
    @GetMapping(value = "/{id}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<MaintenanceStatusHistoryResponseDTO> getMaintenanceStatusHistory(@PathVariable String id) {
        return maintenanceService.getMaintenanceStatusHistory(id)
                .map(this::toHistoryResponse);
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

    private MaintenanceStatusHistoryResponseDTO toHistoryResponse(MaintenanceStatusHistoryEntity entity) {
        return new MaintenanceStatusHistoryResponseDTO(
                entity.id(),
                entity.maintenanceId(),
                entity.status(),
                entity.changedAt().toString(),
                entity.changedBy(),
                entity.comment()
        );
    }

    private record ActiveMaintenanceResponse(String droneId, boolean hasActiveMaintenance) {}
    private record MaintenanceStatusResponse(String code, String description) {}

}