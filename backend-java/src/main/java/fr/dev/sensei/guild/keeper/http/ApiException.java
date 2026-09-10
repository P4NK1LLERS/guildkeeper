package fr.dev.sensei.guild.keeper.http;

/**
 * Erreur destinee a etre rendue telle quelle au client HTTP : porte le code
 * statut et le code d'erreur stable (voir {@code docs/architecture.md} §5.4).
 */
public final class ApiException extends RuntimeException {

    private final int status;
    private final String code;

    private ApiException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ApiException notFound(String message) {
        return new ApiException(404, "NOT_FOUND", message);
    }

    public static ApiException validation(String message) {
        return new ApiException(400, "VALIDATION", message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(409, "CONFLICT", message);
    }

    public static ApiException internal(String message) {
        return new ApiException(500, "INTERNAL", message);
    }

    public int status() {
        return status;
    }

    public String code() {
        return code;
    }
}
