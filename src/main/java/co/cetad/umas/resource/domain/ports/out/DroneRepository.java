package co.cetad.umas.resource.domain.ports.out;

import co.cetad.umas.resource.domain.model.entity.DroneEntity;
import co.cetad.umas.resource.domain.model.vo.DroneStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface DroneRepository {

    /**
     * Obtiene todos los drones
     */
    Flux<DroneEntity> findAll();

    /**
     * Busca un drone por ID
     */
    Mono<Optional<DroneEntity>> findById(String id);

    /**
     * Guarda un nuevo drone
     */
    Mono<DroneEntity> save(DroneEntity drone);

    /**
     * Actualiza un drone existente
     */
    Mono<DroneEntity> update(DroneEntity drone);

    /**
     * Actualiza solo el estado de un drone
     */
    Mono<Void> updateStatus(String id, DroneStatus status);

}