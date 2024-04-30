package fi.vm.yti.common.security;

import java.util.Collection;
import java.util.EnumSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.DCTerms;

import fi.vm.yti.security.AuthenticatedUserProvider;
import fi.vm.yti.security.Role;
import fi.vm.yti.security.YtiUser;

import static fi.vm.yti.security.Role.ADMIN;
import static fi.vm.yti.security.Role.DATA_MODEL_EDITOR;
import static fi.vm.yti.security.Role.TERMINOLOGY_EDITOR;

public class BaseAuthorizationManagerImpl implements BaseAuthorizationManager {

    private final AuthenticatedUserProvider userProvider;

    public BaseAuthorizationManagerImpl(AuthenticatedUserProvider userProvider) {
        this.userProvider = userProvider;
    }

    @Override
    public boolean hasRightToModel(String uri, Model model, Role role) {
        var oldRes = model.getResource(uri);
        var organizations = oldRes.listProperties(DCTerms.contributor).toList().stream().map(prop -> {
            var orgUri = prop.getObject().toString();
            return UUID.fromString(
                    orgUri.substring(
                            orgUri.lastIndexOf(":")+ 1));
        }).collect(Collectors.toSet());
        return hasRightToAnyOrganization(organizations, role);
    }

    @Override
    public boolean isSuperUser() {
        return userProvider.getUser().isSuperuser();
    }

    @Override
    public YtiUser getUser() {
        return userProvider.getUser();
    }

    @Override
    public boolean isAdminOfAnyOrganization(Collection<UUID> organizations) {
        return hasRightToAnyOrganization(organizations, ADMIN);
    }

    @Override
    public boolean isDataModelEditorOfAnyOrganization(Collection<UUID> organizations) {
        return hasRightToAnyOrganization(organizations, DATA_MODEL_EDITOR);
    }

    @Override
    public boolean isTerminologyEditorOfAnyOrganization(Collection<UUID> organizations) {
        return hasRightToAnyOrganization(organizations, TERMINOLOGY_EDITOR);
    }

    @Override
    public boolean hasRightToAnyOrganization(Collection<UUID> organizations, Role...roles) {
        if (organizations.isEmpty()) {
            return false;
        }
        YtiUser user = userProvider.getUser();
        return user.isSuperuser() || user.isInAnyRole(EnumSet.of(ADMIN, roles), organizations);
    }

}
