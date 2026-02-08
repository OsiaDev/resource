package co.cetad.umas.resource.infrastructure.web.controller;

import co.cetad.umas.resource.application.service.piece.PieceService;
import co.cetad.umas.resource.domain.model.dto.PieceCreateRequestDTO;
import co.cetad.umas.resource.domain.model.dto.PieceResponseDTO;
import co.cetad.umas.resource.domain.model.dto.PieceUpdateRequestDTO;
import co.cetad.umas.resource.domain.model.entity.PieceEntity;
import co.cetad.umas.resource.infrastructure.security.RoleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/pieces")
public class PieceController {

    private static final Logger log = LoggerFactory.getLogger(PieceController.class);

    private final PieceService pieceService;

    public PieceController(PieceService pieceService) {
        this.pieceService = pieceService;
    }

    /**
     * Obtiene todas las piezas
     * GET /api/v1/pieces
     * Solo accesible para admin y maintainer
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<PieceResponseDTO> getAllPieces(
            @RequestHeader("X-User-Id") String keycloakUserId,
            @RequestHeader("X-User-Roles") String rolesHeader) {

        log.debug("getAllPieces - User: {}, Roles: {}", keycloakUserId, rolesHeader);

        if (RoleValidator.hasAuthorizedRoleForPieces(rolesHeader)) {
            log.warn("Unauthorized access attempt to getAllPieces by user: {} with roles: {}",
                    keycloakUserId, rolesHeader);
            return Flux.empty();
        }

        return pieceService.getAllPieces()
                .map(this::toResponse);
    }

    /**
     * Obtiene todas las piezas activas
     * GET /api/v1/pieces/active
     * Solo accesible para admin y maintainer
     */
    @GetMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<PieceResponseDTO> getActivePieces(
            @RequestHeader("X-User-Id") String keycloakUserId,
            @RequestHeader("X-User-Roles") String rolesHeader) {

        log.debug("getActivePieces - User: {}, Roles: {}", keycloakUserId, rolesHeader);

        if (RoleValidator.hasAuthorizedRoleForPieces(rolesHeader)) {
            log.warn("Unauthorized access attempt to getActivePieces by user: {} with roles: {}",
                    keycloakUserId, rolesHeader);
            return Flux.empty();
        }

        return pieceService.getActivePieces()
                .map(this::toResponse);
    }

    /**
     * Obtiene una pieza por su ID
     * GET /api/v1/pieces/{id}
     * Solo accesible para admin y maintainer
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PieceResponseDTO>> getPieceById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String keycloakUserId,
            @RequestHeader("X-User-Roles") String rolesHeader) {

        log.debug("getPieceById - User: {}, Roles: {}, PieceId: {}", keycloakUserId, rolesHeader, id);

        if (RoleValidator.hasAuthorizedRoleForPieces(rolesHeader)) {
            log.warn("Unauthorized access attempt to getPieceById by user: {} with roles: {}",
                    keycloakUserId, rolesHeader);
            return Mono.just(ResponseEntity.ok().build());
        }

        return Mono.fromFuture(pieceService.getPieceById(id))
                .map(piece -> ResponseEntity.ok(toResponse(piece)))
                .onErrorResume(e -> {
                    log.error("Error getting piece by id: {}", id, e);
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    /**
     * Crea una nueva pieza
     * POST /api/v1/pieces
     * Solo accesible para admin y maintainer
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PieceResponseDTO>> createPiece(
            @RequestBody PieceCreateRequestDTO request,
            @RequestHeader("X-User-Id") String keycloakUserId,
            @RequestHeader("X-User-Roles") String rolesHeader) {

        log.debug("createPiece - User: {}, Roles: {}, Request: {}", keycloakUserId, rolesHeader, request);

        if (RoleValidator.hasAuthorizedRoleForPieces(rolesHeader)) {
            log.warn("Unauthorized access attempt to createPiece by user: {} with roles: {}",
                    keycloakUserId, rolesHeader);
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }

        return Mono.fromFuture(pieceService.createPiece(request))
                .map(piece -> {
                    log.info("Piece created successfully by user: {}, pieceId: {}", keycloakUserId, piece.id());
                    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(piece));
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.error("Validation error creating piece: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .onErrorResume(e -> {
                    log.error("Error creating piece", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    /**
     * Actualiza una pieza existente
     * PUT /api/v1/pieces/{id}
     * Solo accesible para admin y maintainer
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<PieceResponseDTO>> updatePiece(
            @PathVariable String id,
            @RequestBody PieceUpdateRequestDTO request,
            @RequestHeader("X-User-Id") String keycloakUserId,
            @RequestHeader("X-User-Roles") String rolesHeader) {

        log.debug("updatePiece - User: {}, Roles: {}, PieceId: {}, Request: {}",
                keycloakUserId, rolesHeader, id, request);

        if (RoleValidator.hasAuthorizedRoleForPieces(rolesHeader)) {
            log.warn("Unauthorized access attempt to updatePiece by user: {} with roles: {}",
                    keycloakUserId, rolesHeader);
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }

        return Mono.fromFuture(pieceService.updatePiece(id, request))
                .map(piece -> {
                    log.info("Piece updated successfully by user: {}, pieceId: {}", keycloakUserId, piece.id());
                    return ResponseEntity.ok(toResponse(piece));
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.error("Validation error updating piece: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .onErrorResume(e -> {
                    log.error("Error updating piece: {}", id, e);
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    /**
     * Elimina una pieza por su ID (soft delete)
     * DELETE /api/v1/pieces/{id}
     * Solo accesible para admin y maintainer
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePiece(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String keycloakUserId,
            @RequestHeader("X-User-Roles") String rolesHeader) {

        log.debug("deletePiece - User: {}, Roles: {}, PieceId: {}", keycloakUserId, rolesHeader, id);

        if (RoleValidator.hasAuthorizedRoleForPieces(rolesHeader)) {
            log.warn("Unauthorized access attempt to deletePiece by user: {} with roles: {}",
                    keycloakUserId, rolesHeader);
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }

        return Mono.fromFuture(pieceService.deletePiece(id))
                .then(Mono.fromCallable(() -> {
                    log.info("Piece deleted successfully by user: {}, pieceId: {}", keycloakUserId, id);
                    return ResponseEntity.noContent().<Void>build();
                }))
                .onErrorResume(e -> {
                    log.error("Error deleting piece: {}", id, e);
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    private PieceResponseDTO toResponse(PieceEntity entity) {
        return new PieceResponseDTO(
                entity.id(),
                entity.name(),
                entity.description(),
                entity.active(),
                entity.createdAt().toString(),
                entity.updatedAt().toString()
        );
    }

}