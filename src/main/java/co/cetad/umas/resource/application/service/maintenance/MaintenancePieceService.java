package co.cetad.umas.resource.application.service.maintenance;

import co.cetad.umas.resource.domain.model.dto.MaintenancePieceUpdateDTO;
import co.cetad.umas.resource.domain.model.entity.MaintenancePieceEntity;
import co.cetad.umas.resource.domain.model.entity.PieceEntity;
import co.cetad.umas.resource.domain.ports.out.MaintenancePieceRepository;
import co.cetad.umas.resource.domain.ports.out.PieceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
public class MaintenancePieceService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenancePieceService.class);

    private final MaintenancePieceRepository maintenancePieceRepository;
    private final PieceRepository pieceRepository;

    public MaintenancePieceService(
            MaintenancePieceRepository maintenancePieceRepository,
            PieceRepository pieceRepository) {
        this.maintenancePieceRepository = maintenancePieceRepository;
        this.pieceRepository = pieceRepository;
    }

    /**
     * Obtiene todas las piezas de un mantenimiento con información detallada de cada pieza
     */
    public Flux<MaintenancePieceWithDetails> getMaintenancePiecesWithDetails(String maintenanceId) {
        return maintenancePieceRepository.findByMaintenanceId(maintenanceId)
                .flatMap(maintenancePiece ->
                        pieceRepository.findById(maintenancePiece.pieceId())
                                .map(piece -> new MaintenancePieceWithDetails(maintenancePiece, piece))
                                .onErrorResume(e -> {
                                    logger.warn("Pieza no encontrada: {}", maintenancePiece.pieceId());
                                    return Mono.empty();
                                })
                );
    }

    /**
     * Obtiene una pieza específica de un mantenimiento
     */
    public CompletableFuture<MaintenancePieceEntity> getMaintenancePieceById(String id) {
        return maintenancePieceRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new MaintenancePieceNotFoundException("Pieza de mantenimiento no encontrada con id: " + id)
                ))
                .toFuture();
    }

    /**
     * Actualiza el estado de una pieza en un mantenimiento
     * Permite al usuario ir haciendo la revisión pieza por pieza
     */
    public CompletableFuture<MaintenancePieceEntity> updateMaintenancePieceStatus(
            String id,
            MaintenancePieceUpdateDTO updateDTO) {

        logger.info("Actualizando pieza de mantenimiento {} a estado {}", id, updateDTO.status());

        return maintenancePieceRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new MaintenancePieceNotFoundException("Pieza de mantenimiento no encontrada con id: " + id)
                ))
                .flatMap(existing -> {
                    MaintenancePieceEntity updated = new MaintenancePieceEntity(
                            existing.id(),
                            existing.maintenanceId(),
                            existing.pieceId(),
                            updateDTO.status(),
                            existing.quantity(),
                            updateDTO.notes(),
                            existing.createdAt(),
                            LocalDateTime.now()
                    );

                    return maintenancePieceRepository.update(updated);
                })
                .doOnSuccess(updated ->
                        logger.info("Pieza de mantenimiento {} actualizada a estado {}",
                                updated.id(), updated.status())
                )
                .toFuture();
    }

    /**
     * Record auxiliar para devolver información completa de la pieza
     */
    public record MaintenancePieceWithDetails(
            MaintenancePieceEntity maintenancePiece,
            PieceEntity piece
    ) {
    }

    public static class MaintenancePieceNotFoundException extends RuntimeException {
        public MaintenancePieceNotFoundException(String message) {
            super(message);
        }
    }

}