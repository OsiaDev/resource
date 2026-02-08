package co.cetad.umas.resource.infrastructure.persistence.repository;

import co.cetad.umas.resource.domain.model.entity.PieceEntity;
import co.cetad.umas.resource.domain.ports.out.PieceRepository;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Repository
public class PieceR2dbcRepository implements PieceRepository {

    private final DatabaseClient databaseClient;

    public PieceR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<PieceEntity> findAll() {
        String sql = """
            SELECT id, name, description, active, created_at, updated_at
            FROM piece
            ORDER BY created_at DESC
            """;

        return databaseClient.sql(sql)
                .map(this::mapRowToEntity)
                .all();
    }

    @Override
    public Flux<PieceEntity> findAllActive() {
        String sql = """
            SELECT id, name, description, active, created_at, updated_at
            FROM piece
            WHERE active = true
            ORDER BY created_at DESC
            """;

        return databaseClient.sql(sql)
                .map(this::mapRowToEntity)
                .all();
    }

    @Override
    public Mono<PieceEntity> findById(String id) {
        String sql = """
            SELECT id, name, description, active, created_at, updated_at
            FROM piece
            WHERE id = :id
            """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(id))
                .map(this::mapRowToEntity)
                .one();
    }

    @Override
    public Mono<PieceEntity> save(PieceEntity piece) {
        String sql = """
            INSERT INTO piece (id, name, description, active, created_at, updated_at)
            VALUES (:id, :name, :description, :active, :createdAt, :updatedAt)
            RETURNING id, name, description, active, created_at, updated_at
            """;

        LocalDateTime now = LocalDateTime.now();

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(piece.id()))
                .bind("name", piece.name())
                .bind("description", piece.description() != null ? piece.description() : "")
                .bind("active", piece.active())
                .bind("createdAt", now)
                .bind("updatedAt", now)
                .map(this::mapRowToEntity)
                .one();
    }

    @Override
    public Mono<PieceEntity> update(PieceEntity piece) {
        String sql = """
            UPDATE piece
            SET name = :name,
                description = :description,
                active = :active,
                updated_at = :updatedAt
            WHERE id = :id
            RETURNING id, name, description, active, created_at, updated_at
            """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(piece.id()))
                .bind("name", piece.name())
                .bind("description", piece.description() != null ? piece.description() : "")
                .bind("active", piece.active())
                .bind("updatedAt", LocalDateTime.now())
                .map(this::mapRowToEntity)
                .one();
    }

    @Override
    public Mono<Void> deleteById(String id) {
        String sql = """
            UPDATE piece
            SET active = false,
                updated_at = :updatedAt 
            WHERE id = :id 
            """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(id))
                .bind("updatedAt", LocalDateTime.now())
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        String sql = "SELECT COUNT(*) as count FROM piece WHERE name = :name";

        return databaseClient.sql(sql)
                .bind("name", name)
                .map(row -> {
                    Long count = row.get("count", Long.class);
                    return count != null && count > 0;
                })
                .one()
                .defaultIfEmpty(false);
    }

    private PieceEntity mapRowToEntity(Readable row) {
        return new PieceEntity(
                Objects.requireNonNull(row.get("id", UUID.class)).toString(),
                row.get("name", String.class),
                row.get("description", String.class),
                row.get("active", Boolean.class),
                row.get("created_at", LocalDateTime.class),
                row.get("updated_at", LocalDateTime.class)
        );
    }

}