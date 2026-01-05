package co.cetad.umas.resource.infrastructure.persistence.repository;

import co.cetad.umas.resource.domain.model.entity.OperatorEntity;
import co.cetad.umas.resource.domain.model.vo.OperatorStatus;
import co.cetad.umas.resource.domain.ports.out.OperatorRepository;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Repository
public class OperatorR2dbcRepository implements OperatorRepository {

    private final DatabaseClient databaseClient;

    public OperatorR2dbcRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<OperatorEntity> findAll() {
        String sql = """
            SELECT id, username, full_name, email, phone_number, ugcs_user_id, 
                   keycloak_user_id, status, is_available, created_at, updated_at
            FROM operator
            ORDER BY created_at DESC
            """;

        return databaseClient.sql(sql)
                .map(this::mapRowToEntity)
                .all();
    }

    @Override
    public Mono<OperatorEntity> findById(String id) {
        String sql = """
            SELECT id, username, full_name, email, phone_number, ugcs_user_id, 
                   keycloak_user_id, status, is_available, created_at, updated_at
            FROM operator
            WHERE id = :id
            """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(id))
                .map(this::mapRowToEntity)
                .one();
    }

    @Override
    public Mono<OperatorEntity> save(OperatorEntity operator) {
        String sql = """
            INSERT INTO operator (id, username, full_name, email, phone_number, ugcs_user_id, 
                                  keycloak_user_id, status, is_available, created_at, updated_at)
            VALUES (:id, :username, :fullName, :email, :phoneNumber, :ugcsUserId, 
                    :keycloakUserId, :status, :isAvailable, :createdAt, :updatedAt)
            RETURNING id, username, full_name, email, phone_number, ugcs_user_id, 
                      keycloak_user_id, status, is_available, created_at, updated_at
            """;

        LocalDateTime now = LocalDateTime.now();

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(operator.id()))
                .bind("username", operator.username())
                .bind("fullName", operator.fullName())
                .bind("email", operator.email())
                .bind("phoneNumber", operator.phoneNumber() != null ? operator.phoneNumber() : "")
                .bind("ugcsUserId", operator.ugcsUserId() != null ? operator.ugcsUserId() : "")
                .bind("keycloakUserId", operator.keycloakUserId() != null ?
                        UUID.fromString(operator.keycloakUserId()) : null)
                .bind("status", operator.status().name())
                .bind("isAvailable", operator.isAvailable() != null ? operator.isAvailable() : true)
                .bind("createdAt", now)
                .bind("updatedAt", now)
                .map((row, metadata) -> mapRowToEntity(row))
                .one();
    }

    @Override
    public Mono<OperatorEntity> update(OperatorEntity operator) {
        String sql = """
            UPDATE operator
            SET username = :username,
                full_name = :fullName,
                email = :email,
                phone_number = :phoneNumber,
                ugcs_user_id = :ugcsUserId,
                keycloak_user_id = :keycloakUserId,
                status = :status,
                is_available = :isAvailable,
                updated_at = :updatedAt
            WHERE id = :id
            RETURNING id, username, full_name, email, phone_number, ugcs_user_id, 
                      keycloak_user_id, status, is_available, created_at, updated_at
            """;

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(operator.id()))
                .bind("username", operator.username())
                .bind("fullName", operator.fullName())
                .bind("email", operator.email())
                .bind("phoneNumber", operator.phoneNumber() != null ? operator.phoneNumber() : "")
                .bind("ugcsUserId", operator.ugcsUserId() != null ? operator.ugcsUserId() : "")
                .bind("keycloakUserId", operator.keycloakUserId() != null ?
                        UUID.fromString(operator.keycloakUserId()) : null)
                .bind("status", operator.status().name())
                .bind("isAvailable", operator.isAvailable())
                .bind("updatedAt", LocalDateTime.now())
                .map(this::mapRowToEntity)
                .one();
    }

    @Override
    public Mono<Void> deleteById(String id) {
        String sql = "DELETE FROM operator WHERE id = :id";

        return databaseClient.sql(sql)
                .bind("id", UUID.fromString(id))
                .fetch()
                .rowsUpdated()
                .then();
    }

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        String sql = "SELECT COUNT(*) as count FROM operator WHERE username = :username";

        return databaseClient.sql(sql)
                .bind("username", username)
                .map(row -> {
                    Long count = row.get("count", Long.class);
                    return count != null && count > 0;
                })
                .one()
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        String sql = "SELECT COUNT(*) as count FROM operator WHERE email = :email";

        return databaseClient.sql(sql)
                .bind("email", email)
                .map(row -> {
                    Long count = row.get("count", Long.class);
                    return count != null && count > 0;
                })
                .one()
                .defaultIfEmpty(false);
    }

    private OperatorEntity mapRowToEntity(Readable row) {
        UUID keycloakUuid = row.get("keycloak_user_id", UUID.class);

        return new OperatorEntity(
                Objects.requireNonNull(row.get("id", UUID.class)).toString(),
                row.get("username", String.class),
                row.get("full_name", String.class),
                row.get("email", String.class),
                row.get("phone_number", String.class),
                row.get("ugcs_user_id", String.class),
                keycloakUuid != null ? keycloakUuid.toString() : null,
                OperatorStatus.valueOf(Objects.requireNonNull(row.get("status", String.class))),
                row.get("is_available", Boolean.class),
                row.get("created_at", LocalDateTime.class),
                row.get("updated_at", LocalDateTime.class)
        );
    }

}