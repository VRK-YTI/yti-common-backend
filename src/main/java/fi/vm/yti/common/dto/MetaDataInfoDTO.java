package fi.vm.yti.common.dto;

import fi.vm.yti.common.enums.GraphType;
import fi.vm.yti.common.enums.Status;

import java.util.Map;
import java.util.Set;

/**
 * Base DTO class for displaying graph's metadata
 */
public class MetaDataInfoDTO extends ResourceCommonInfoDTO {

    private String prefix;
    private GraphType graphType;
    private Set<String> languages = Set.of();
    private Map<String, String> description = Map.of();
    private Status status;
    private Set<OrganizationDTO> organizations = Set.of();
    private Set<ServiceCategoryDTO> groups = Set.of();
    private String contact;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public GraphType getModelType() {
        return graphType;
    }

    public void setModelType(GraphType graphType) {
        this.graphType = graphType;
    }

    public Set<String> getLanguages() {
        return languages;
    }

    public void setLanguages(Set<String> languages) {
        this.languages = languages;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public Set<OrganizationDTO> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(Set<OrganizationDTO> organizations) {
        this.organizations = organizations;
    }

    public Set<ServiceCategoryDTO> getGroups() {
        return groups;
    }

    public void setGroups(Set<ServiceCategoryDTO> groups) {
        this.groups = groups;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

}
