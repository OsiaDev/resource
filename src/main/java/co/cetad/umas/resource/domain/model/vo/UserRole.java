package co.cetad.umas.resource.domain.model.vo;

import lombok.Getter;

@Getter
public enum UserRole {

    ADMIN("admin"),
    MAINTAINER("maintainer");

    private final String role;

    UserRole(String role) {
        this.role = role;
    }

    public static boolean isAuthorizedForPieces(String role) {
        return ADMIN.role.equalsIgnoreCase(role) ||
                MAINTAINER.role.equalsIgnoreCase(role);
    }

}