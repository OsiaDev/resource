package co.cetad.umas.resource.application.service.drone;

import co.cetad.umas.resource.domain.model.dto.DroneCreateRequestDTO;
import co.cetad.umas.resource.domain.model.dto.DroneUpdateRequestDTO;
import co.cetad.umas.resource.domain.model.entity.DroneEntity;
import co.cetad.umas.resource.domain.model.vo.DroneStatus;
import co.cetad.umas.resource.domain.ports.out.DroneRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class DroneService {

    private final DroneRepository droneRepository;

    public DroneService(DroneRepository droneRepository) {
        this.droneRepository = droneRepository;
    }

    /**
     * Obtiene todos los drones
     */
    public Flux<DroneEntity> getAllDrones() {
        return droneRepository.findAll();
    }

    /**
     * Obtiene todos los drones activos
     */
    public Flux<DroneEntity> getActiveDrones() {
        return droneRepository.findByStatus(DroneStatus.ACTIVE);
    }

    /**
     * Obtiene un drone por ID
     */
    public CompletableFuture<DroneEntity> getDroneById(String id) {
        return droneRepository.findById(id)
                .toFuture()
                .thenApply(drone -> drone.orElseThrow(() ->
                        new DroneNotFoundException("Drone not found with id: " + id)));
    }

    /**
     * Crea un nuevo drone
     */
    public CompletableFuture<DroneEntity> createDrone(DroneCreateRequestDTO request) {
        return CompletableFuture.supplyAsync(() -> {
            LocalDateTime now = LocalDateTime.now();
            return new DroneEntity(
                    UUID.randomUUID().toString(),
                    request.name(),
                    request.vehicleId(),
                    request.model(),
                    request.description(),
                    request.serialNumber(),
                    DroneStatus.ACTIVE,
                    request.flightHours(),
                    now,
                    now
            );
        }).thenCompose(drone ->
                droneRepository.save(drone).toFuture()
        );
    }

    /**
     * Actualiza un drone existente
     */
    public CompletableFuture<DroneEntity> updateDrone(String id, DroneUpdateRequestDTO request) {
        return getDroneById(id)
                .thenApply(existingDrone -> new DroneEntity(
                        existingDrone.id(),
                        request.name(),
                        request.vehicleId(),
                        request.model(),
                        request.description(),
                        request.serialNumber(),
                        // Preservar el status existente si no se proporciona uno nuevo
                        request.status() != null ? request.status() : existingDrone.status(),
                        request.flightHours(),
                        existingDrone.createdAt(),
                        LocalDateTime.now()
                ))
                .thenCompose(updatedDrone ->
                        droneRepository.update(updatedDrone).toFuture()
                );
    }

    /**
     * Actualiza solo el estado de un drone
     */
    public CompletableFuture<DroneEntity> updateDroneStatus(String id, DroneStatus status) {
        return getDroneById(id)
                .thenApply(existingDrone -> new DroneEntity(
                        existingDrone.id(),
                        existingDrone.name(),
                        existingDrone.vehicleId(),
                        existingDrone.model(),
                        existingDrone.description(),
                        existingDrone.serialNumber(),
                        status,
                        existingDrone.flightHours(),
                        existingDrone.createdAt(),
                        LocalDateTime.now()
                ))
                .thenCompose(updatedDrone ->
                        droneRepository.updateStatus(updatedDrone.id(), updatedDrone.status())
                                .thenReturn(updatedDrone)
                                .toFuture()
                );
    }

    /**
     * Elimina un drone (soft delete - cambia estado a DECOMMISSIONED)
     */
    public CompletableFuture<Void> deleteDrone(String id) {
        return updateDroneStatus(id, DroneStatus.DECOMMISSIONED)
                .thenApply(v -> null);
    }

    public static class DroneNotFoundException extends RuntimeException {
        public DroneNotFoundException(String message) {
            super(message);
        }
    }

}