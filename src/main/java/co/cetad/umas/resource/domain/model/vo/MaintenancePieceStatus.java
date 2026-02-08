package co.cetad.umas.resource.domain.model.vo;

import lombok.Getter;

@Getter
public enum MaintenancePieceStatus {

    PENDING("Pendiente"),
    CHECKED("Revisada - OK"),
    REPLACED("Reemplazada"),
    DAMAGED("Dañada");

    private final String status;

    MaintenancePieceStatus(String status) {
        this.status = status;
    }

}