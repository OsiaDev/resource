package co.cetad.umas.resource.infrastructure.persistence.repository;

import co.cetad.umas.resource.domain.model.entity.MaintenanceEntity;
import co.cetad.umas.resource.domain.model.vo.MaintenanceStatus;
import co.cetad.umas.resource.domain.ports.out.MaintenanceRepository;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Repository
public class MaintenanceR2dbcRepository implements MaintenanceRepository {

    private final DatabaseClient databaseClient;

    public MaintenanceR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<MaintenanceEntity> findAll() {
        String sql = """
            SELECT id, drone_id, current_status, description, created_at, updated_at
            FROM maintenance
            ORDER BY created_at DESC
            """;

        return databaseClient.sql(sql)
                .map(this::mapRowToEntity)
                .all();
    }

    @Override
    public Mono<MaintenanceEntity> findById(String id) {
        String sql = """
            SELECT id, drone_id, current_status, description, created_at, updated_at
            FROM maintenance
            WHERE id = :id
            """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(id))
                .map(this::mapRowToEntity)
                .one();
    }

    @Override
    public Flux<MaintenanceEntity> findByDroneId(String droneId) {
        String sql = """
            SELECT id, drone_id, current_status, description, created_at, updated_at
            FROM maintenance
            WHERE drone_id = :droneId
            ORDER BY created_at DESC
            """;

        return databaseClient.sql(sql)
                .bind("droneId", UUID.fromString(droneId))
                .map(this::mapRowToEntity)
                .all();
    }

    @Override
    public Flux<MaintenanceEntity> findByStatus(MaintenanceStatus status) {
        String sql = """
            SELECT id, drone_id, current_status, description, created_at, updated_at
            FROM maintenance
            WHERE current_status = :status::maintenance_status
            ORDER BY created_at DESC
            """;

        return databaseClient.sql(sql)
                .bind("status", status.name())
                .map(this::mapRowToEntity)
                .all();
    }

    @Override
    public Mono<MaintenanceEntity> save(MaintenanceEntity maintenance) {
        String sql = """
            INSERT INTO maintenance (id, drone_id, current_status, description, created_at, updated_at)
            VALUES (:id, :droneId, :currentStatus::maintenance_status, :description, :createdAt, :updatedAt)
            RETURNING id, drone_id, current_status, description, created_at, updated_at
            """;

        LocalDateTime now = LocalDateTime.now();

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(maintenance.id()))
                .bind("droneId", UUID.fromString(maintenance.droneId()))
                .bind("currentStatus", maintenance.currentStatus().name())
                .bind("description", maintenance.description() != null ? maintenance.description() : "")
                .bind("createdAt", now)
                .bind("updatedAt", now)
                .map(this::mapRowToEntity)
                .one();
    }

    @Override
    public Mono<MaintenanceEntity> update(MaintenanceEntity maintenance) {
        String sql = """
            UPDATE maintenance
            SET current_status = :currentStatus::maintenance_status,
                description = :description,
                updated_at = :updatedAt
            WHERE id = :id
            RETURNING id, drone_id, current_status, description, created_at, updated_at
            """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(maintenance.id()))
                .bind("currentStatus", maintenance.currentStatus().name())
                .bind("description", maintenance.description() != null ? maintenance.description() : "")
                .bind("updatedAt", LocalDateTime.now())
                .map(this::mapRowToEntity)
                .one();
    }

    @Override
    public Mono<Boolean> existsActiveMaintenanceForDrone(String droneId) {
        String sql = """
            SELECT COUNT(*) as count 
            FROM maintenance 
            WHERE drone_id = :droneId 
            AND current_status IN ('ACTIVE', 'IN_MAINTENANCE', 'REPAIRING', 'OUT_OF_SERVICE')
            """;

        return databaseClient.sql(sql)
                .bind("droneId", UUID.fromString(droneId))
                .map(row -> {
                    Long count = row.get("count", Long.class);
                    return count != null && count > 0;
                })
                .one()
                .defaultIfEmpty(false);
    }

    private MaintenanceEntity mapRowToEntity(Readable row) {
        return new MaintenanceEntity(
                Objects.requireNonNull(row.get("id", UUID.class)).toString(),
                Objects.requireNonNull(row.get("drone_id", UUID.class)).toString(),
                MaintenanceStatus.valueOf(Objects.requireNonNull(row.get("current_status", String.class))),
                row.get("description", String.class),
                row.get("created_at", LocalDateTime.class),
                row.get("updated_at", LocalDateTime.class)
        );
    }

}