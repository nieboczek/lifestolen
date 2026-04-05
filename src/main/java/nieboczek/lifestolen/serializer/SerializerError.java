package nieboczek.lifestolen.serializer;

public class SerializerError extends RuntimeException {
    public SerializerError(String message) {
        super(message);
    }

    public SerializerError(String message, Throwable cause) {
        super(message, cause);
    }
}
