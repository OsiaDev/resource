package co.cetad.umas.resource.domain.ports.out;

import co.cetad.umas.resource.domain.model.entity.MaintenanceStatusHistoryEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MaintenanceStatusHistoryRepository {

    /**
     * Obtiene todo el historial de estados de un mantenimiento
     */
    Flux<MaintenanceStatusHistoryEntity> findByMaintenanceId(String maintenanceId);

    /**
     * Guarda un nuevo registro en el historial
     */
    Mono<MaintenanceStatusHistoryEntity> save(MaintenanceStatusHistoryEntity history);

}