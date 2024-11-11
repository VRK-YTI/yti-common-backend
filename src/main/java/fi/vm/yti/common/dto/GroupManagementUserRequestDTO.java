package fi.vm.yti.common.dto;

import java.util.List;
import java.util.UUID;

public class GroupManagementUserRequestDTO {
    private UUID organizationId;
    private List<String> role;

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public List<String> getRole() {
        return role;
    }

    public void setRole(List<String> role) {
        this.role = role;
    }
}