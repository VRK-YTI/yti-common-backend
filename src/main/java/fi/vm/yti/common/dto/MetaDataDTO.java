package fi.vm.yti.common.dto;

import fi.vm.yti.common.enums.GraphType;
import fi.vm.yti.common.enums.Status;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Base DTO class for storing graph (terminology or data model) metadata
 */
public class MetaDataDTO {

    private String prefix;
    private Map<String, String> label = Map.of();
    private Map<String, String> description = Map.of();
    private GraphType graphType;
    private Set<String> languages = Set.of();
    private Status status;
    private Set<UUID> organizations = Set.of();
    private Set<String> groups = Set.of();
    private String contact;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Map<String, String> getLabel() {
        return label;
    }

    public void setLabel(Map<String, String> label) {
        this.label = label;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public GraphType getGraphType() {
        return graphType;
    }

    public void setGraphType(GraphType graphType) {
        this.graphType = graphType;
    }

    public Set<String> getLanguages() {
        return languages;
    }

    public void setLanguages(Set<String> languages) {
        this.languages = languages;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<UUID> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(Set<UUID> organizations) {
        this.organizations = organizations;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
