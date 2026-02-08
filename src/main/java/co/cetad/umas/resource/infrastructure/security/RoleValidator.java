package co.cetad.umas.resource.infrastructure.security;

import co.cetad.umas.resource.domain.model.vo.UserRole;

import java.util.Arrays;
import java.util.List;

public class RoleValidator {

    private RoleValidator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Verifica si el usuario tiene al menos uno de los roles autorizados para acceder a piezas
     * @param rolesHeader Header con los roles separados por coma
     * @return true si tiene rol admin o maintainer
     */
    public static boolean hasAuthorizedRoleForPieces(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return true;
        }

        List<String> userRoles = extractRoles(rolesHeader);

        return !userRoles.contains(UserRole.ADMIN.getRole()) &&
                !userRoles.contains(UserRole.MAINTAINER.getRole());
    }

    /**
     * Extrae los roles del header y los convierte en una lista
     * @param rolesHeader Header con los roles separados por coma
     * @return Lista de roles
     */
    public static List<String> extractRoles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return List.of();
        }

        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .toList();
    }

}