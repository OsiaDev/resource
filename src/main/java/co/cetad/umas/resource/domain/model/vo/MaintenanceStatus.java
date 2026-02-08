package co.cetad.umas.resource.domain.model.vo;

import lombok.Getter;

@Getter
public enum MaintenanceStatus {

    ACTIVE("Activo"),
    IN_MAINTENANCE("En mantenimiento"),
    REPAIRING("En reparación"),
    OUT_OF_SERVICE("Fuera de servicio"),
    COMPLETED("Completado");

    private final String status;

    MaintenanceStatus(String status) {
        this.status = status;
    }

}