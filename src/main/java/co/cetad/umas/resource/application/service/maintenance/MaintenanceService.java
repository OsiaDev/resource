package co.cetad.umas.resource.application.service.maintenance;

import co.cetad.umas.resource.domain.model.dto.MaintenanceCreateRequestDTO;
import co.cetad.umas.resource.domain.model.dto.MaintenanceUpdateRequestDTO;
import co.cetad.umas.resource.domain.model.entity.MaintenanceEntity;
import co.cetad.umas.resource.domain.model.vo.DroneStatus;
import co.cetad.umas.resource.domain.model.vo.MaintenanceStatus;
import co.cetad.umas.resource.domain.ports.out.DroneRepository;
import co.cetad.umas.resource.domain.ports.out.MaintenanceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class MaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final DroneRepository droneRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository, DroneRepository droneRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.droneRepository = droneRepository;
    }

    /**
     * Obtiene todos los mantenimientos
     */
    public Flux<MaintenanceEntity> getAllMaintenances() {
        return maintenanceRepository.findAll();
    }

    /**
     * Obtiene un mantenimiento por ID
     */
    public CompletableFuture<MaintenanceEntity> getMaintenanceById(String id) {
        return maintenanceRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new MaintenanceNotFoundException("Maintenance not found with id: " + id)
                ))
                .toFuture();
    }

    /**
     * Obtiene todos los mantenimientos de un drone específico
     */
    public Flux<MaintenanceEntity> getMaintenancesByDroneId(String droneId) {
        return maintenanceRepository.findByDroneId(droneId);
    }

    /**
     * Obtiene todos los mantenimientos por estado
     */
    public Flux<MaintenanceEntity> getMaintenancesByStatus(MaintenanceStatus status) {
        return maintenanceRepository.findByStatus(status);
    }

    /**
     * Crea un nuevo mantenimiento y actualiza el estado del drone a IN_MAINTENANCE
     */
    public CompletableFuture<MaintenanceEntity> createMaintenance(MaintenanceCreateRequestDTO request) {
        return validateDroneExists(request.droneId())
                .flatMap(droneExists -> {
                    if (!droneExists) {
                        return Mono.error(
                                new IllegalArgumentException("Drone not found with id: " + request.droneId())
                        );
                    }

                    // Crear el nuevo mantenimiento
                    MaintenanceEntity newMaintenance = new MaintenanceEntity(
                            UUID.randomUUID().toString(),
                            request.droneId(),
                            MaintenanceStatus.ACTIVE,
                            request.description(),
                            null,
                            null
                    );

                    // Guardar el mantenimiento y actualizar el estado del drone
                    return maintenanceRepository.save(newMaintenance)
                            .flatMap(savedMaintenance ->
                                    droneRepository.updateStatus(request.droneId(), DroneStatus.IN_MAINTENANCE)
                                            .thenReturn(savedMaintenance)
                            );
                })
                .toFuture();
    }

    /**
     * Actualiza un mantenimiento existente
     * Si el estado cambia a COMPLETED, actualiza el drone a ACTIVE
     */
    public CompletableFuture<MaintenanceEntity> updateMaintenance(String id, MaintenanceUpdateRequestDTO request) {
        return maintenanceRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new MaintenanceNotFoundException("Maintenance not found with id: " + id)
                ))
                .flatMap(existing -> {
                    MaintenanceEntity updatedMaintenance = new MaintenanceEntity(
                            existing.id(),
                            existing.droneId(),
                            request.currentStatus() != null ? request.currentStatus() : existing.currentStatus(),
                            request.description() != null ? request.description() : existing.description(),
                            existing.createdAt(),
                            null
                    );

                    Mono<MaintenanceEntity> updateMono = maintenanceRepository.update(updatedMaintenance);

                    // Si el estado cambia a COMPLETED, actualizar el drone a ACTIVE
                    if (request.currentStatus() == MaintenanceStatus.COMPLETED &&
                            existing.currentStatus() != MaintenanceStatus.COMPLETED) {
                        return updateMono.flatMap(updated ->
                                droneRepository.updateStatus(updated.droneId(), DroneStatus.ACTIVE)
                                        .thenReturn(updated)
                        );
                    }

                    return updateMono;
                })
                .toFuture();
    }

    /**
     * Verifica si existe un mantenimiento activo para un drone
     */
    public CompletableFuture<Boolean> hasActiveMaintenance(String droneId) {
        return maintenanceRepository.existsActiveMaintenanceForDrone(droneId)
                .toFuture();
    }

    /**
     * Valida que el drone exista
     */
    private Mono<Boolean> validateDroneExists(String droneId) {
        return droneRepository.findById(droneId)
                .map(Optional::isPresent)
                .defaultIfEmpty(false);
    }

    public static class MaintenanceNotFoundException extends RuntimeException {
        public MaintenanceNotFoundException(String message) {
            super(message);
        }
    }

}