package co.cetad.umas.resource.domain.ports.out;

import co.cetad.umas.resource.domain.model.entity.DroneEntity;
import reactor.core.publisher.Flux;

public interface DroneRepository {

    /**
     * Obtiene todos los drones
     */
    Flux<DroneEntity> findAll();

}
