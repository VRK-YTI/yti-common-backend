package fi.vm.yti.common.util;

public abstract class GraphURI {

    private String prefix;
    private String resourceId;
    private String version;

    public void createModelURI(String prefix, String version) {
        this.prefix = prefix;
        this.version = version;
    }

    public void createResourceURI(String prefix, String resourceId, String version) {
        this.prefix = prefix;
        this.resourceId = resourceId;
        this.version = version;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getVersion() {
        return version;
    }

    public String getResourceURI() {
        if (this.resourceId == null) {
            return null;
        }
        return getGraphURI() + this.resourceId;
    }

    public abstract String getGraphURI();

    public abstract String getModelResourceURI();

}
