package co.cetad.umas.resource.domain.model.vo;

import lombok.Getter;

@Getter
public enum MaintenancePieceStatus {

    PENDING("Pendiente"),
    CHECKED("Chequeado"),
    REPLACED("Reemplazado"),
    DAMAGED("Dañado");

    private final String status;

    MaintenancePieceStatus(String status) {
        this.status = status;
    }
}
