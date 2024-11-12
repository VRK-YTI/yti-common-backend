package fi.vm.yti.common.exception;

public class ResourceExistsException extends RuntimeException {

    public ResourceExistsException(String identifier, String graphURI) {
        super(String.format("Resource %s already exists in graph %s", identifier, graphURI));
    }
}
