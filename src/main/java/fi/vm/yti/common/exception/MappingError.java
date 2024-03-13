package fi.vm.yti.common.exception;

public class MappingError extends RuntimeException {

    public MappingError(String message) {
        super("Error during mapping: " + message);
    }
}
