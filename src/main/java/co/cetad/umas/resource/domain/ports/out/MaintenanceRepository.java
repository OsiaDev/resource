package co.cetad.umas.resource.domain.ports.out;

import co.cetad.umas.resource.domain.model.entity.MaintenanceEntity;
import co.cetad.umas.resource.domain.model.vo.MaintenanceStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MaintenanceRepository {

    /**
     * Obtiene todos los mantenimientos
     */
    Flux<MaintenanceEntity> findAll();

    /**
     * Obtiene un mantenimiento por su ID
     */
    Mono<MaintenanceEntity> findById(String id);

    /**
     * Obtiene todos los mantenimientos de un drone específico
     */
    Flux<MaintenanceEntity> findByDroneId(String droneId);

    /**
     * Obtiene todos los mantenimientos por estado
     */
    Flux<MaintenanceEntity> findByStatus(MaintenanceStatus status);

    /**
     * Guarda un nuevo mantenimiento
     */
    Mono<MaintenanceEntity> save(MaintenanceEntity maintenance);

    /**
     * Actualiza un mantenimiento existente
     */
    Mono<MaintenanceEntity> update(MaintenanceEntity maintenance);

    /**
     * Verifica si existe un mantenimiento activo para un drone
     */
    Mono<Boolean> existsActiveMaintenanceForDrone(String droneId);

}