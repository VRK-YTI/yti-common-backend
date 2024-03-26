package fi.vm.yti.common.security;

import fi.vm.yti.security.YtiUser;
import org.apache.jena.rdf.model.Model;

import fi.vm.yti.security.Role;

import java.util.Collection;
import java.util.UUID;

public interface BaseAuthorizationManager {

    boolean hasRightToModel(String prefix, Model model, Role role);

    boolean hasRightToAnyOrganization(Collection<UUID> organizations, Role... role);

    boolean isAdminOfAnyOrganization(Collection<UUID> organizations);

    boolean isDataModelEditorOfAnyOrganization(Collection<UUID> organizations);

    boolean isTerminologyEditorOfAnyOrganization(Collection<UUID> organizations);

    boolean isSuperUser();

    YtiUser getUser();
}
