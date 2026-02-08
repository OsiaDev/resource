package co.cetad.umas.resource.infrastructure.persistence.repository;

import co.cetad.umas.resource.domain.model.entity.MaintenanceStatusHistoryEntity;
import co.cetad.umas.resource.domain.model.vo.MaintenanceStatus;
import co.cetad.umas.resource.domain.ports.out.MaintenanceStatusHistoryRepository;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Repository
public class MaintenanceStatusHistoryR2dbcRepository implements MaintenanceStatusHistoryRepository {

    private final DatabaseClient databaseClient;

    public MaintenanceStatusHistoryR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<MaintenanceStatusHistoryEntity> findByMaintenanceId(String maintenanceId) {
        String sql = """
            SELECT id, maintenance_id, status, changed_at, changed_by, comment
            FROM maintenance_status_history
            WHERE maintenance_id = :maintenanceId
            ORDER BY changed_at DESC
            """;

        return databaseClient.sql(sql)
                .bind("maintenanceId", UUID.fromString(maintenanceId))
                .map(this::mapRowToEntity)
                .all();
    }

    @Override
    public Mono<MaintenanceStatusHistoryEntity> save(MaintenanceStatusHistoryEntity history) {
        String sql = """
            INSERT INTO maintenance_status_history (id, maintenance_id, status, changed_at, changed_by, comment)
            VALUES (:id, :maintenanceId, :status::maintenance_status, :changedAt, :changedBy, :comment)
            RETURNING id, maintenance_id, status, changed_at, changed_by, comment
            """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(history.id()))
                .bind("maintenanceId", UUID.fromString(history.maintenanceId()))
                .bind("status", history.status().name())
                .bind("changedAt", history.changedAt())
                .bind("changedBy", history.changedBy() != null ? UUID.fromString(history.changedBy()) : null)
                .bind("comment", history.comment() != null ? history.comment() : "")
                .map(this::mapRowToEntity)
                .one();
    }

    private MaintenanceStatusHistoryEntity mapRowToEntity(Readable row) {
        UUID changedBy = row.get("changed_by", UUID.class);

        return new MaintenanceStatusHistoryEntity(
                Objects.requireNonNull(row.get("id", UUID.class)).toString(),
                Objects.requireNonNull(row.get("maintenance_id", UUID.class)).toString(),
                MaintenanceStatus.valueOf(Objects.requireNonNull(row.get("status", String.class))),
                row.get("changed_at", LocalDateTime.class),
                changedBy != null ? changedBy.toString() : null,
                row.get("comment", String.class)
        );
    }

}