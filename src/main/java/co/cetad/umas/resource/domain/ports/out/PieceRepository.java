package co.cetad.umas.resource.domain.ports.out;

import co.cetad.umas.resource.domain.model.entity.PieceEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PieceRepository {

    /**
     * Obtiene todas las piezas
     */
    Flux<PieceEntity> findAll();

    /**
     * Obtiene todas las piezas activas
     */
    Flux<PieceEntity> findAllActive();

    /**
     * Obtiene una pieza por su ID
     */
    Mono<PieceEntity> findById(String id);

    /**
     * Guarda una nueva pieza
     */
    Mono<PieceEntity> save(PieceEntity piece);

    /**
     * Actualiza una pieza existente
     */
    Mono<PieceEntity> update(PieceEntity piece);

    /**
     * Elimina una pieza por su ID (soft delete - cambia active a false)
     */
    Mono<Void> deleteById(String id);

    /**
     * Verifica si existe una pieza con el nombre dado
     */
    Mono<Boolean> existsByName(String name);

}