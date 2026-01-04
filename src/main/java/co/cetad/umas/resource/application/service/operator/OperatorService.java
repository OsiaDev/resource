package co.cetad.umas.resource.application.service.operator;

import co.cetad.umas.resource.domain.model.dto.OperatorRequestDTO;
import co.cetad.umas.resource.domain.model.entity.OperatorEntity;
import co.cetad.umas.resource.domain.model.vo.OperatorStatus;
import co.cetad.umas.resource.domain.ports.out.OperatorRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class OperatorService {

    private final OperatorRepository operatorRepository;

    public OperatorService(OperatorRepository operatorRepository) {
        this.operatorRepository = operatorRepository;
    }

    /**
     * Obtiene todos los operadores
     */
    public Flux<OperatorEntity> getAllOperators() {
        return operatorRepository.findAll();
    }

    /**
     * Obtiene un operador por su ID usando CompletableFuture
     */
    public CompletableFuture<OperatorEntity> getOperatorById(String id) {
        return operatorRepository.findById(id)
                .toFuture();
    }

    /**
     * Crea un nuevo operador
     */
    public CompletableFuture<OperatorEntity> createOperator(OperatorRequestDTO request) {
        return validateUniqueFields(null, request.username(), request.email())
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(
                                new IllegalArgumentException("Username o email ya existe")
                        );
                    }

                    OperatorEntity newOperator = new OperatorEntity(
                            UUID.randomUUID().toString(),
                            request.username(),
                            request.fullName(),
                            request.email(),
                            request.phoneNumber(),
                            request.ugcsUserId(),
                            request.keycloakUserId(),
                            request.status() != null ? request.status() : OperatorStatus.ACTIVE,
                            request.isAvailable() != null ? request.isAvailable() : true,
                            null,
                            null
                    );

                    return operatorRepository.save(newOperator);
                })
                .toFuture();
    }

    /**
     * Actualiza un operador existente
     */
    public CompletableFuture<OperatorEntity> updateOperator(String id, OperatorRequestDTO request) {
        return operatorRepository.findById(id)
                .flatMap(existing ->
                        validateUniqueFields(id, request.username(), request.email())
                                .flatMap(valid -> {
                                    if (!valid) {
                                        return Mono.error(
                                                new IllegalArgumentException("Username o email ya existe")
                                        );
                                    }

                                    OperatorEntity updatedOperator = new OperatorEntity(
                                            existing.id(),
                                            request.username(),
                                            request.fullName(),
                                            request.email(),
                                            request.phoneNumber(),
                                            request.ugcsUserId(),
                                            request.keycloakUserId(),
                                            request.status() != null ? request.status() : existing.status(),
                                            request.isAvailable() != null ? request.isAvailable() : existing.isAvailable(),
                                            existing.createdAt(),
                                            null
                                    );

                                    return operatorRepository.update(updatedOperator);
                                })
                )
                .toFuture();
    }

    /**
     * Elimina un operador por su ID
     */
    public CompletableFuture<Void> deleteOperator(String id) {
        return operatorRepository.findById(id)
                .flatMap(existing -> operatorRepository.deleteById(id))
                .toFuture();
    }

    /**
     * Válida que el username y email sean únicos
     */
    private Mono<Boolean> validateUniqueFields(String excludeId, String username, String email) {
        Mono<Boolean> usernameCheck = operatorRepository.existsByUsername(username)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.just(true);
                    }
                    if (excludeId == null) {
                        return Mono.just(false);
                    }
                    return operatorRepository.findById(excludeId)
                            .map(existing -> existing.username().equals(username))
                            .defaultIfEmpty(false);
                });

        Mono<Boolean> emailCheck = operatorRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.just(true);
                    }
                    if (excludeId == null) {
                        return Mono.just(false);
                    }
                    return operatorRepository.findById(excludeId)
                            .map(existing -> existing.email().equals(email))
                            .defaultIfEmpty(false);
                });

        return Mono.zip(usernameCheck, emailCheck)
                .map(tuple -> tuple.getT1() && tuple.getT2());
    }

}