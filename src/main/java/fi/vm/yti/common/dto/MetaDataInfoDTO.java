package fi.vm.yti.common.dto;

import java.util.Map;
import java.util.Set;

/**
 * Base DTO class fot displaying graph's metadata
 */
public class MetaDataInfoDTO extends ResourceCommonInfoDTO {

    private String prefix;
    private Status status;
    private Map<String, String> description = Map.of();
    private Set<String> languages = Set.of();
    private Set<OrganizationDTO> organizations = Set.of();
    private Set<ServiceCategoryDTO> groups = Set.of();
    private String contact;
    private Map<String, String> documentation = Map.of();
    private Set<LinkDTO> links = Set.of();

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

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public Set<String> getLanguages() {
        return languages;
    }

    public void setLanguages(Set<String> languages) {
        this.languages = languages;
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

    public Map<String, String> getDocumentation() {
        return documentation;
    }

    public void setDocumentation(Map<String, String> documentation) {
        this.documentation = documentation;
    }

    public Set<LinkDTO> getLinks() {
        return links;
    }

    public void setLinks(Set<LinkDTO> links) {
        this.links = links;
    }
}
