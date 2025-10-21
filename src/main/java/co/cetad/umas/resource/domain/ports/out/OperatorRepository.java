package co.cetad.umas.resource.domain.ports.out;

import co.cetad.umas.resource.domain.model.entity.OperatorEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OperatorRepository {

    /**
     * Obtiene todos los operadores
     */
    Flux<OperatorEntity> findAll();

    /**
     * Obtiene un operador por su ID
     */
    Mono<OperatorEntity> findById(String id);

    /**
     * Guarda un nuevo operador
     */
    Mono<OperatorEntity> save(OperatorEntity operator);

    /**
     * Actualiza un operador existente
     */
    Mono<OperatorEntity> update(OperatorEntity operator);

    /**
     * Elimina un operador por su ID
     */
    Mono<Void> deleteById(String id);

    /**
     * Verifica si existe un operador con el username dado
     */
    Mono<Boolean> existsByUsername(String username);

    /**
     * Verifica si existe un operador con el email dado
     */
    Mono<Boolean> existsByEmail(String email);

}