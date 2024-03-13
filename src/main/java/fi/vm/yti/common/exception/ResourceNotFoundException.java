package fi.vm.yti.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String prefix) {
        super("Resource not found: " + prefix);
    }
}
