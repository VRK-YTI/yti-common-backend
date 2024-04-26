package fi.vm.yti.common.opensearch;

import com.fasterxml.jackson.annotation.JsonFormat;
import fi.vm.yti.common.enums.GraphType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IndexMetaData extends IndexBase {

    private String contentModified;
    private GraphType type;
    private String prefix;
    private Map<String, String> description;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<UUID> organizations;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> groups;
    private List<String> languages;

    public String getContentModified() {
        return contentModified;
    }

    public void setContentModified(String contentModified) {
        this.contentModified = contentModified;
    }

    public GraphType getType() {
        return type;
    }

    public void setType(GraphType type) {
        this.type = type;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Map<String, String> getDescription() {
        return description;
    }

    @Override
    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public List<UUID> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<UUID> organizations) {
        this.organizations = organizations;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }
}
