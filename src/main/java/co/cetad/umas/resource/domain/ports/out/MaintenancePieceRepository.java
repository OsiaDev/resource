package co.cetad.umas.resource.domain.ports.out;

import co.cetad.umas.resource.domain.model.entity.MaintenancePieceEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MaintenancePieceRepository {

    /**
     * Obtiene todas las piezas de un mantenimiento
     */
    Flux<MaintenancePieceEntity> findByMaintenanceId(String maintenanceId);

    /**
     * Obtiene una pieza específica de un mantenimiento
     */
    Mono<MaintenancePieceEntity> findById(String id);

    /**
     * Guarda una nueva relación maintenance-piece
     */
    Mono<MaintenancePieceEntity> save(MaintenancePieceEntity maintenancePiece);

    /**
     * Guarda múltiples relaciones maintenance-piece en batch
     */
    Flux<MaintenancePieceEntity> saveAll(Flux<MaintenancePieceEntity> maintenancePieces);

    /**
     * Actualiza una relación maintenance-piece existente
     */
    Mono<MaintenancePieceEntity> update(MaintenancePieceEntity maintenancePiece);

    /**
     * Elimina todas las piezas de un mantenimiento
     */
    Mono<Void> deleteByMaintenanceId(String maintenanceId);

}