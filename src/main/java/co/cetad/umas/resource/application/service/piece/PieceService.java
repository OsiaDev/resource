package co.cetad.umas.resource.application.service.piece;

import co.cetad.umas.resource.domain.model.dto.PieceCreateRequestDTO;
import co.cetad.umas.resource.domain.model.dto.PieceUpdateRequestDTO;
import co.cetad.umas.resource.domain.model.entity.PieceEntity;
import co.cetad.umas.resource.domain.ports.out.PieceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PieceService {

    private final PieceRepository pieceRepository;

    public PieceService(PieceRepository pieceRepository) {
        this.pieceRepository = pieceRepository;
    }

    /**
     * Obtiene todas las piezas
     */
    public Flux<PieceEntity> getAllPieces() {
        return pieceRepository.findAll();
    }

    /**
     * Obtiene todas las piezas activas
     */
    public Flux<PieceEntity> getActivePieces() {
        return pieceRepository.findAllActive();
    }

    /**
     * Obtiene una pieza por su ID usando CompletableFuture
     */
    public CompletableFuture<PieceEntity> getPieceById(String id) {
        return pieceRepository.findById(id)
                .switchIfEmpty(Mono.error(new PieceNotFoundException("Piece not found with id: " + id)))
                .toFuture();
    }

    /**
     * Crea una nueva pieza
     */
    public CompletableFuture<PieceEntity> createPiece(PieceCreateRequestDTO request) {
        return validateUniqueName(null, request.name())
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(
                                new IllegalArgumentException("El nombre de la pieza ya existe")
                        );
                    }

                    PieceEntity newPiece = new PieceEntity(
                            UUID.randomUUID().toString(),
                            request.name(),
                            request.description(),
                            request.active() != null ? request.active() : true,
                            null,
                            null
                    );

                    return pieceRepository.save(newPiece);
                })
                .toFuture();
    }

    /**
     * Actualiza una pieza existente
     */
    public CompletableFuture<PieceEntity> updatePiece(String id, PieceUpdateRequestDTO request) {
        return pieceRepository.findById(id)
                .switchIfEmpty(Mono.error(new PieceNotFoundException("Piece not found with id: " + id)))
                .flatMap(existing ->
                        validateUniqueName(id, request.name())
                                .flatMap(valid -> {
                                    if (!valid) {
                                        return Mono.error(
                                                new IllegalArgumentException("El nombre de la pieza ya existe")
                                        );
                                    }

                                    PieceEntity updatedPiece = new PieceEntity(
                                            existing.id(),
                                            request.name(),
                                            request.description(),
                                            request.active() != null ? request.active() : existing.active(),
                                            existing.createdAt(),
                                            null
                                    );

                                    return pieceRepository.update(updatedPiece);
                                })
                )
                .toFuture();
    }

    /**
     * Elimina una pieza por su ID (soft delete)
     */
    public CompletableFuture<Void> deletePiece(String id) {
        return pieceRepository.findById(id)
                .switchIfEmpty(Mono.error(new PieceNotFoundException("Piece not found with id: " + id)))
                .flatMap(existing -> pieceRepository.deleteById(id))
                .toFuture();
    }

    /**
     * Válida que el nombre sea único
     */
    private Mono<Boolean> validateUniqueName(String excludeId, String name) {
        return pieceRepository.existsByName(name)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.just(true);
                    }
                    if (excludeId == null) {
                        return Mono.just(false);
                    }
                    return pieceRepository.findById(excludeId)
                            .map(existing -> existing.name().equals(name))
                            .defaultIfEmpty(false);
                });
    }

    public static class PieceNotFoundException extends RuntimeException {
        public PieceNotFoundException(String message) {
            super(message);
        }
    }

}