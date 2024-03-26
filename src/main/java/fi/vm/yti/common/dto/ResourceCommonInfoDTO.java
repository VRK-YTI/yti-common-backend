package fi.vm.yti.common.dto;

import java.util.Map;

/**
 * Base DTO class for displaying resources data
 */
public class ResourceCommonInfoDTO {

    private String uri;
    private Map<String, String> label;
    private String created;
    private String modified;
    private UserDTO modifier;
    private UserDTO creator;

    public Map<String, String> getLabel() {
        return label;
    }

    public void setLabel(Map<String, String> label) {
        this.label = label;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public UserDTO getModifier() {
        return modifier;
    }

    public void setModifier(UserDTO modifier) {
        this.modifier = modifier;
    }

    public UserDTO getCreator() {
        return creator;
    }

    public void setCreator(UserDTO creator) {
        this.creator = creator;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
