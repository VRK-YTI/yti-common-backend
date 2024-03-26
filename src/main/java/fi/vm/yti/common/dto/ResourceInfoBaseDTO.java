package fi.vm.yti.common.dto;

import java.util.Set;

public class ResourceInfoBaseDTO extends ResourceCommonInfoDTO {

    private String editorialNote;
    private Status status;
    private String identifier;
    private Set<OrganizationDTO> contributor;
    private String contact;

    public String getEditorialNote() {
        return editorialNote;
    }

    public void setEditorialNote(String editorialNote) {
        this.editorialNote = editorialNote;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Set<OrganizationDTO> getContributor() {
        return contributor;
    }

    public void setContributor(Set<OrganizationDTO> contributor) {
        this.contributor = contributor;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
