package co.cetad.umas.resource.infrastructure.persistence.repository;

import co.cetad.umas.resource.domain.model.entity.DroneEntity;
import co.cetad.umas.resource.domain.model.vo.DroneStatus;
import co.cetad.umas.resource.domain.ports.out.DroneRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import io.r2dbc.spi.Readable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
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
        SELECT id, name, vehicle_id, model, description, serial_number, status, flight_hours, created_at, updated_at
        FROM drone
        ORDER BY created_at DESC
        """;

        return databaseClient.sql(sql)
                .map((row, metadata) -> mapRowToDroneEntity(row))
                .all();
    }

    @Override
    public Mono<Optional<DroneEntity>> findById(String id) {
        String sql = """
        SELECT id, name, vehicle_id, model, description, serial_number, status, flight_hours, created_at, updated_at
        FROM drone
        WHERE id = :id
        """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(id))
                .map((row, metadata) -> mapRowToDroneEntity(row))
                .one()
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty());
    }

    @Override
    public Mono<DroneEntity> save(DroneEntity drone) {
        String sql = """
        INSERT INTO drone (id, name, vehicle_id, model, description, serial_number, status, flight_hours, created_at, updated_at)
        VALUES (:id, :name, :vehicleId, :model, :description, :serialNumber, :status::drone_status, :flightHours, :createdAt, :updatedAt)
        RETURNING id, name, vehicle_id, model, description, serial_number, status, flight_hours, created_at, updated_at
        """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(drone.id()))
                .bind("name", drone.name())
                .bind("vehicleId", drone.vehicleId())
                .bind("model", drone.model())
                .bind("description", drone.description())
                .bind("serialNumber", drone.serialNumber())
                .bind("status", drone.status().name())
                .bind("flightHours", drone.flightHours())
                .bind("createdAt", drone.createdAt())
                .bind("updatedAt", drone.updatedAt())
                .map((row, metadata) -> mapRowToDroneEntity(row))
                .one();
    }

    @Override
    public Mono<DroneEntity> update(DroneEntity drone) {
        String sql = """
        UPDATE drone
        SET name = :name,
            vehicle_id = :vehicleId,
            model = :model,
            description = :description,
            serial_number = :serialNumber,
            status = :status::drone_status,
            flight_hours = :flightHours,
            updated_at = :updatedAt
        WHERE id = :id
        RETURNING id, name, vehicle_id, model, description, serial_number, status, flight_hours, created_at, updated_at
        """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(drone.id()))
                .bind("name", drone.name())
                .bind("vehicleId", drone.vehicleId())
                .bind("model", drone.model())
                .bind("description", drone.description())
                .bind("serialNumber", drone.serialNumber())
                .bind("status", drone.status().name())
                .bind("flightHours", drone.flightHours())
                .bind("updatedAt", drone.updatedAt())
                .map((row, metadata) -> mapRowToDroneEntity(row))
                .one();
    }

    @Override
    public Mono<Void> updateStatus(String id, DroneStatus status) {
        String sql = """
        UPDATE drone
        SET status = :status::drone_status,
            updated_at = :updatedAt
        WHERE id = :id
        """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(id))
                .bind("status", status.name())
                .bind("updatedAt", LocalDateTime.now())
                .fetch()
                .rowsUpdated()
                .then();
    }

    private DroneEntity mapRowToDroneEntity(Readable row) {
        return new DroneEntity(
                Objects.requireNonNull(row.get("id", UUID.class)).toString(),
                row.get("name", String.class),
                row.get("vehicle_id", String.class),
                row.get("model", String.class),
                row.get("description", String.class),
                row.get("serial_number", String.class),
                DroneStatus.valueOf(Objects.requireNonNull(row.get("status", String.class))),
                row.get("flight_hours", BigDecimal.class),
                row.get("created_at", LocalDateTime.class),
                row.get("updated_at", LocalDateTime.class)
        );
    }

}