package fi.vm.yti.common.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

public class GroupManagementOrganizationDTO {
    private String uuid;
    private Map<String,String> prefLabel;
    private Map<String,String> description;
    private String url;

    private String parentId;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Map<String, String> getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(Map<String, String> prefLabel) {
        this.prefLabel = prefLabel;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
