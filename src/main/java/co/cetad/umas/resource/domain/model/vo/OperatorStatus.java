package co.cetad.umas.resource.domain.model.vo;

import lombok.Getter;

@Getter
public enum OperatorStatus {

    ACTIVE("Activo"),
    INACTIVE("Inactivo"),
    SUSPENDED("Suspendido");

    private final String status;

    OperatorStatus(String status) {
        this.status = status;
    }

}