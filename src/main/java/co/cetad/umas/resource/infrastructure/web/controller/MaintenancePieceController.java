package co.cetad.umas.resource.infrastructure.web.controller;

import co.cetad.umas.resource.application.service.maintenance.MaintenancePieceService;
import co.cetad.umas.resource.domain.model.dto.MaintenancePieceResponseDTO;
import co.cetad.umas.resource.domain.model.dto.MaintenancePieceUpdateDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/maintenance-pieces")
public class MaintenancePieceController {

    private final MaintenancePieceService maintenancePieceService;

    public MaintenancePieceController(MaintenancePieceService maintenancePieceService) {
        this.maintenancePieceService = maintenancePieceService;
    }

    /**
     * Obtiene todas las piezas de un mantenimiento con información detallada
     * GET /api/v1/maintenance-pieces/maintenance/{maintenanceId}
     */
    @GetMapping(value = "/maintenance/{maintenanceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<MaintenancePieceResponseDTO> getMaintenancePieces(@PathVariable String maintenanceId) {
        return maintenancePieceService.getMaintenancePiecesWithDetails(maintenanceId)
                .map(details -> new MaintenancePieceResponseDTO(
                        details.maintenancePiece().id(),
                        details.maintenancePiece().maintenanceId(),
                        details.maintenancePiece().pieceId(),
                        details.piece().name(),
                        details.piece().code(),
                        details.maintenancePiece().status(),
                        details.maintenancePiece().quantity(),
                        details.maintenancePiece().notes(),
                        details.maintenancePiece().createdAt().toString(),
                        details.maintenancePiece().updatedAt().toString()
                ));
    }

    /**
     * Actualiza el estado de una pieza en un mantenimiento
     * Permite al usuario ir haciendo la revisión pieza por pieza
     * PUT /api/v1/maintenance-pieces/{id}
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<MaintenancePieceResponseDTO> updateMaintenancePiece(
            @PathVariable String id,
            @RequestBody MaintenancePieceUpdateDTO updateDTO) {

        return Mono.fromFuture(
                maintenancePieceService.updateMaintenancePieceStatus(id, updateDTO)
                        .thenApply(entity -> new MaintenancePieceResponseDTO(
                                entity.id(),
                                entity.maintenanceId(),
                                entity.pieceId(),
                                null, // Se puede obtener en un segundo llamado si es necesario
                                null,
                                entity.status(),
                                entity.quantity(),
                                entity.notes(),
                                entity.createdAt().toString(),
                                entity.updatedAt().toString()
                        ))
        );
    }

}