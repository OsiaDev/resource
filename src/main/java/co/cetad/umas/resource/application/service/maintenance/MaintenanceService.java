package co.cetad.umas.resource.application.service.maintenance;

import co.cetad.umas.resource.domain.model.dto.MaintenanceCreateRequestDTO;
import co.cetad.umas.resource.domain.model.dto.MaintenanceStatusUpdateDTO;
import co.cetad.umas.resource.domain.model.entity.MaintenanceEntity;
import co.cetad.umas.resource.domain.model.entity.MaintenancePieceEntity;
import co.cetad.umas.resource.domain.model.entity.MaintenanceStatusHistoryEntity;
import co.cetad.umas.resource.domain.model.vo.DroneStatus;
import co.cetad.umas.resource.domain.model.vo.MaintenancePieceStatus;
import co.cetad.umas.resource.domain.model.vo.MaintenanceStatus;
import co.cetad.umas.resource.domain.ports.out.DroneRepository;
import co.cetad.umas.resource.domain.ports.out.MaintenancePieceRepository;
import co.cetad.umas.resource.domain.ports.out.MaintenanceRepository;
import co.cetad.umas.resource.domain.ports.out.MaintenanceStatusHistoryRepository;
import co.cetad.umas.resource.domain.ports.out.PieceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class MaintenanceService {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceService.class);

    private final MaintenanceRepository maintenanceRepository;
    private final MaintenancePieceRepository maintenancePieceRepository;
    private final MaintenanceStatusHistoryRepository statusHistoryRepository;
    private final PieceRepository pieceRepository;
    private final DroneRepository droneRepository;

    public MaintenanceService(
            MaintenanceRepository maintenanceRepository,
            MaintenancePieceRepository maintenancePieceRepository,
            MaintenanceStatusHistoryRepository statusHistoryRepository,
            PieceRepository pieceRepository,
            DroneRepository droneRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.maintenancePieceRepository = maintenancePieceRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.pieceRepository = pieceRepository;
        this.droneRepository = droneRepository;
    }

    /**
     * Obtiene todos los mantenimientos
     */
    public Flux<MaintenanceEntity> getAllMaintenances() {
        return maintenanceRepository.findAll();
    }

    /**
     * Obtiene un mantenimiento por ID
     */
    public CompletableFuture<MaintenanceEntity> getMaintenanceById(String id) {
        return maintenanceRepository.findById(id)
                .switchIfEmpty(Mono.error(new MaintenanceNotFoundException("Mantenimiento no encontrado con id: " + id)))
                .toFuture();
    }

    /**
     * Obtiene todos los mantenimientos de un drone
     */
    public Flux<MaintenanceEntity> getMaintenancesByDroneId(String droneId) {
        return maintenanceRepository.findByDroneId(droneId);
    }

    /**
     * Obtiene todos los mantenimientos por estado
     */
    public Flux<MaintenanceEntity> getMaintenancesByStatus(MaintenanceStatus status) {
        return maintenanceRepository.findByStatus(status);
    }

    /**
     * Crea un nuevo mantenimiento y automáticamente crea registros de piezas en estado PENDING
     * También cambia el estado del drone a IN_MAINTENANCE
     */
    public CompletableFuture<MaintenanceEntity> createMaintenance(MaintenanceCreateRequestDTO request) {
        logger.info("Creando mantenimiento para drone: {}", request.droneId());

        return CompletableFuture.supplyAsync(() -> {
            String maintenanceId = UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();

            MaintenanceEntity newMaintenance = new MaintenanceEntity(
                    maintenanceId,
                    request.droneId(),
                    request.operatorId(),
                    MaintenanceStatus.SCHEDULED,
                    request.description(),
                    request.scheduledDate(),
                    null,
                    null,
                    request.notes(),
                    now,
                    now
            );
            return newMaintenance;
        }).thenCompose(maintenance ->
                // 1. Guardar el mantenimiento
                maintenanceRepository.save(maintenance)
                        .flatMap(savedMaintenance -> {
                            logger.info("Mantenimiento creado: {}", savedMaintenance.id());

                            // 2. Crear registro inicial en historial
                            return createStatusHistory(
                                    savedMaintenance.id(),
                                    MaintenanceStatus.SCHEDULED,
                                    request.operatorId(),
                                    "Mantenimiento creado"
                            ).thenReturn(savedMaintenance);
                        })
                        .flatMap(savedMaintenance -> {
                            // 3. Obtener todas las piezas y crear registros en estado PENDING
                            return pieceRepository.findAll()
                                    .map(piece -> {
                                        String maintenancePieceId = UUID.randomUUID().toString();
                                        LocalDateTime now = LocalDateTime.now();

                                        return new MaintenancePieceEntity(
                                                maintenancePieceId,
                                                savedMaintenance.id(),
                                                piece.id(),
                                                MaintenancePieceStatus.PENDING,
                                                1,
                                                null,
                                                now,
                                                now
                                        );
                                    })
                                    .collectList()
                                    .flatMapMany(maintenancePieceRepository::saveAll)
                                    .collectList()
                                    .doOnSuccess(pieces ->
                                            logger.info("Creados {} registros de piezas para mantenimiento {}",
                                                    pieces.size(), savedMaintenance.id())
                                    )
                                    .thenReturn(savedMaintenance);
                        })
                        .flatMap(savedMaintenance -> {
                            // 4. Cambiar estado del drone a IN_MAINTENANCE
                            return droneRepository.updateStatus(savedMaintenance.droneId(), DroneStatus.IN_MAINTENANCE)
                                    .doOnSuccess(v ->
                                            logger.info("Estado del drone {} cambiado a IN_MAINTENANCE",
                                                    savedMaintenance.droneId())
                                    )
                                    .thenReturn(savedMaintenance);
                        })
                        .toFuture()
        );
    }

    /**
     * Actualiza el estado de un mantenimiento
     * Si el estado cambia a COMPLETED, automáticamente cambia el estado del drone a ACTIVE
     * Siempre crea un registro en el historial de estados
     */
    public CompletableFuture<MaintenanceEntity> updateMaintenanceStatus(
            String id,
            MaintenanceStatusUpdateDTO statusUpdate) {

        logger.info("Actualizando estado de mantenimiento {} a {}", id, statusUpdate.status());

        return maintenanceRepository.findById(id)
                .switchIfEmpty(Mono.error(new MaintenanceNotFoundException("Mantenimiento no encontrado con id: " + id)))
                .flatMap(existing -> {
                    LocalDateTime now = LocalDateTime.now();

                    // Actualizar campos según el nuevo estado
                    LocalDateTime startDate = existing.startDate();
                    LocalDateTime endDate = existing.endDate();

                    if (statusUpdate.status() == MaintenanceStatus.IN_PROGRESS && startDate == null) {
                        startDate = now;
                    }

                    if (statusUpdate.status() == MaintenanceStatus.COMPLETED && endDate == null) {
                        endDate = now;
                    }

                    MaintenanceEntity updatedMaintenance = new MaintenanceEntity(
                            existing.id(),
                            existing.droneId(),
                            existing.operatorId(),
                            statusUpdate.status(),
                            existing.description(),
                            existing.scheduledDate(),
                            startDate,
                            endDate,
                            existing.notes(),
                            existing.createdAt(),
                            now
                    );

                    return maintenanceRepository.update(updatedMaintenance);
                })
                .flatMap(updated -> {
                    // Crear registro en historial
                    return createStatusHistory(
                            updated.id(),
                            updated.status(),
                            statusUpdate.changedBy(),
                            statusUpdate.comment()
                    ).thenReturn(updated);
                })
                .flatMap(updated -> {
                    // Si el estado es COMPLETED, cambiar drone a ACTIVE
                    if (updated.status() == MaintenanceStatus.COMPLETED) {
                        logger.info("Mantenimiento completado, cambiando drone {} a ACTIVE", updated.droneId());

                        return droneRepository.updateStatus(updated.droneId(), DroneStatus.ACTIVE)
                                .doOnSuccess(v ->
                                        logger.info("Estado del drone {} cambiado a ACTIVE", updated.droneId())
                                )
                                .thenReturn(updated);
                    }
                    return Mono.just(updated);
                })
                .toFuture();
    }

    /**
     * Elimina un mantenimiento
     */
    public CompletableFuture<Void> deleteMaintenance(String id) {
        return maintenanceRepository.findById(id)
                .switchIfEmpty(Mono.error(new MaintenanceNotFoundException("Mantenimiento no encontrado con id: " + id)))
                .flatMap(existing ->
                        // Primero eliminar las piezas asociadas
                        maintenancePieceRepository.deleteByMaintenanceId(id)
                                .then(maintenanceRepository.deleteById(id))
                )
                .toFuture();
    }

    /**
     * Obtiene el historial de estados de un mantenimiento
     */
    public Flux<MaintenanceStatusHistoryEntity> getMaintenanceStatusHistory(String maintenanceId) {
        return statusHistoryRepository.findByMaintenanceId(maintenanceId);
    }

    /**
     * Método auxiliar para crear un registro en el historial de estados
     */
    private Mono<MaintenanceStatusHistoryEntity> createStatusHistory(
            String maintenanceId,
            MaintenanceStatus status,
            String changedBy,
            String comment) {

        MaintenanceStatusHistoryEntity history = new MaintenanceStatusHistoryEntity(
                UUID.randomUUID().toString(),
                maintenanceId,
                status,
                LocalDateTime.now(),
                changedBy,
                comment
        );

        return statusHistoryRepository.save(history)
                .doOnSuccess(saved ->
                        logger.info("Registro de historial creado para mantenimiento {}: {}",
                                maintenanceId, status)
                );
    }

    public static class MaintenanceNotFoundException extends RuntimeException {
        public MaintenanceNotFoundException(String message) {
            super(message);
        }
    }

}