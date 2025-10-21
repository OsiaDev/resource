package co.cetad.umas.resource.infrastructure.web.controller;

import co.cetad.umas.resource.application.service.operator.OperatorService;
import co.cetad.umas.resource.domain.model.dto.OperatorRequestDTO;
import co.cetad.umas.resource.domain.model.dto.OperatorResponseDTO;
import co.cetad.umas.resource.domain.model.entity.OperatorEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/operators")
public class OperatorController {

    private final OperatorService operatorService;

    public OperatorController(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    /**
     * Obtiene todos los operadores
     * GET /api/v1/operators
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<OperatorResponseDTO> getAllOperators() {
        return operatorService.getAllOperators()
                .map(this::toResponse);
    }

    /**
     * Obtiene un operador por su ID
     * GET /api/v1/operators/{id}
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<OperatorResponseDTO>> getOperatorById(@PathVariable String id) {
        return Mono.fromFuture(operatorService.getOperatorById(id))
                .map(operator -> ResponseEntity.ok(toResponse(operator)))
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Crea un nuevo operador
     * POST /api/v1/operators
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<OperatorResponseDTO>> createOperator(@RequestBody OperatorRequestDTO request) {
        return Mono.fromFuture(operatorService.createOperator(request))
                .map(operator -> ResponseEntity.status(HttpStatus.CREATED).body(toResponse(operator)))
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.just(ResponseEntity.badRequest().build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * Actualiza un operador existente
     * PUT /api/v1/operators/{id}
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<OperatorResponseDTO>> updateOperator(
            @PathVariable String id,
            @RequestBody OperatorRequestDTO request) {
        return Mono.fromFuture(operatorService.updateOperator(id, request))
                .map(operator -> ResponseEntity.ok(toResponse(operator)))
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.just(ResponseEntity.badRequest().build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    /**
     * Elimina un operador por su ID
     * DELETE /api/v1/operators/{id}
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteOperator(@PathVariable String id) {
        return Mono.fromFuture(operatorService.deleteOperator(id))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    private OperatorResponseDTO toResponse(OperatorEntity entity) {
        return new OperatorResponseDTO(
                entity.id(),
                entity.username(),
                entity.fullName(),
                entity.email(),
                entity.phoneNumber(),
                entity.ugcsUserId(),
                entity.status(),
                entity.isAvailable(),
                entity.createdAt().toString(),
                entity.updatedAt().toString()
        );
    }

}