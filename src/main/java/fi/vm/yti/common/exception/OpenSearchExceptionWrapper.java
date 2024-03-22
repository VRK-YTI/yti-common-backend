package fi.vm.yti.common.exception;

public class OpenSearchExceptionWrapper extends RuntimeException {
    
    private final String index;

    public OpenSearchExceptionWrapper(String message, String index){
        super(message);
        this.index = index;
    }

    public String getIndex() {
        return index;
    }
}
