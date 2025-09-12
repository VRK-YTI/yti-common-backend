package fi.vm.yti.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final String uri;

    public ResourceNotFoundException(String uri) {
        super("resource-not-found");
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }
}
