package co.cetad.umas.resource.application.service.drone;

import co.cetad.umas.resource.domain.model.entity.DroneEntity;
import co.cetad.umas.resource.domain.ports.out.DroneRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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

}
