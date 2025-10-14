package co.cetad.umas.resource.infrastructure.persistence.repository;

import co.cetad.umas.resource.domain.model.entity.DroneEntity;
import co.cetad.umas.resource.domain.model.vo.DroneStatus;
import co.cetad.umas.resource.domain.ports.out.DroneRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Repository
public class DroneR2dbcRepository implements DroneRepository {

    private final DatabaseClient databaseClient;

    public DroneR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<DroneEntity> findAll() {
        String sql = """
        SELECT id, vehicle_id, model, description, serial_number, status, created_at, updated_at
        FROM drone
        ORDER BY created_at DESC
        """;

        return databaseClient.sql(sql)
                .map(row -> new DroneEntity(
                        Objects.requireNonNull(row.get("id", UUID.class)).toString(),
                        row.get("vehicle_id", String.class),
                        row.get("model", String.class),
                        row.get("description", String.class),
                        row.get("serial_number", String.class),
                        // Convertimos el string de DB a DroneStatus
                        DroneStatus.valueOf(Objects.requireNonNull(row.get("status", String.class))),
                        row.get("created_at", LocalDateTime.class),
                        row.get("updated_at", LocalDateTime.class)
                ))
                .all();
    }


}
