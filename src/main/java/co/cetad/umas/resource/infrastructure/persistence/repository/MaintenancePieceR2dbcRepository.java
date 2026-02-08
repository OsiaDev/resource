package co.cetad.umas.resource.infrastructure.persistence.repository;

import co.cetad.umas.resource.domain.model.entity.MaintenancePieceEntity;
import co.cetad.umas.resource.domain.model.vo.MaintenancePieceStatus;
import co.cetad.umas.resource.domain.ports.out.MaintenancePieceRepository;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Repository
public class MaintenancePieceR2dbcRepository implements MaintenancePieceRepository {

    private final DatabaseClient databaseClient;

    public MaintenancePieceR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<MaintenancePieceEntity> findByMaintenanceId(String maintenanceId) {
        String sql = """
            SELECT id, maintenance_id, piece_id, status, quantity, notes, created_at, updated_at
            FROM maintenance_piece
            WHERE maintenance_id = :maintenanceId
            ORDER BY created_at ASC
            """;

        return databaseClient.sql(sql)
                .bind("maintenanceId", UUID.fromString(maintenanceId))
                .map(this::mapRowToEntity)
                .all();
    }

    @Override
    public Mono<MaintenancePieceEntity> findById(String id) {
        String sql = """
            SELECT id, maintenance_id, piece_id, status, quantity, notes, created_at, updated_at
            FROM maintenance_piece
            WHERE id = :id
            """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(id))
                .map(this::mapRowToEntity)
                .one();
    }

    @Override
    public Mono<MaintenancePieceEntity> save(MaintenancePieceEntity maintenancePiece) {
        // IMPORTANTE: El enum en PostgreSQL se llama maintenance_part_status, no maintenance_piece_status
        String sql = """
            INSERT INTO maintenance_piece (id, maintenance_id, piece_id, status, quantity, notes, created_at, updated_at)
            VALUES (:id, :maintenanceId, :pieceId, :status::maintenance_part_status, :quantity, :notes, :createdAt, :updatedAt)
            RETURNING id, maintenance_id, piece_id, status, quantity, notes, created_at, updated_at
            """;

        LocalDateTime now = LocalDateTime.now();

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(maintenancePiece.id()))
                .bind("maintenanceId", UUID.fromString(maintenancePiece.maintenanceId()))
                .bind("pieceId", UUID.fromString(maintenancePiece.pieceId()))
                .bind("status", maintenancePiece.status().name())
                .bind("quantity", maintenancePiece.quantity())
                .bind("notes", maintenancePiece.notes() != null ? maintenancePiece.notes() : "")
                .bind("createdAt", now)
                .bind("updatedAt", now)
                .map(this::mapRowToEntity)
                .one();
    }

    @Override
    public Flux<MaintenancePieceEntity> saveAll(Flux<MaintenancePieceEntity> maintenancePieces) {
        return maintenancePieces.flatMap(this::save);
    }

    @Override
    public Mono<MaintenancePieceEntity> update(MaintenancePieceEntity maintenancePiece) {
        // IMPORTANTE: El enum en PostgreSQL se llama maintenance_part_status, no maintenance_piece_status
        String sql = """
            UPDATE maintenance_piece
            SET status = :status::maintenance_part_status,
                quantity = :quantity,
                notes = :notes,
                updated_at = :updatedAt
            WHERE id = :id
            RETURNING id, maintenance_id, piece_id, status, quantity, notes, created_at, updated_at
            """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(maintenancePiece.id()))
                .bind("status", maintenancePiece.status().name())
                .bind("quantity", maintenancePiece.quantity())
                .bind("notes", maintenancePiece.notes() != null ? maintenancePiece.notes() : "")
                .bind("updatedAt", LocalDateTime.now())
                .map(this::mapRowToEntity)
                .one();
    }

    @Override
    public Mono<Void> deleteByMaintenanceId(String maintenanceId) {
        String sql = "DELETE FROM maintenance_piece WHERE maintenance_id = :maintenanceId";

        return databaseClient.sql(sql)
                .bind("maintenanceId", UUID.fromString(maintenanceId))
                .fetch()
                .rowsUpdated()
                .then();
    }

    private MaintenancePieceEntity mapRowToEntity(Readable row) {
        return new MaintenancePieceEntity(
                Objects.requireNonNull(row.get("id", UUID.class)).toString(),
                Objects.requireNonNull(row.get("maintenance_id", UUID.class)).toString(),
                Objects.requireNonNull(row.get("piece_id", UUID.class)).toString(),
                MaintenancePieceStatus.valueOf(Objects.requireNonNull(row.get("status", String.class))),
                row.get("quantity", Integer.class),
                row.get("notes", String.class),
                row.get("created_at", LocalDateTime.class),
                row.get("updated_at", LocalDateTime.class)
        );
    }

}