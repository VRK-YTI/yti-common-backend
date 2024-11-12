package fi.vm.yti.common.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fi.vm.yti.common.Constants;
import fi.vm.yti.common.dto.GroupManagementOrganizationDTO;
import fi.vm.yti.common.dto.GroupManagementUserDTO;
import fi.vm.yti.common.dto.GroupManagementUserRequestDTO;
import fi.vm.yti.common.dto.ResourceCommonInfoDTO;
import fi.vm.yti.common.properties.SuomiMeta;
import fi.vm.yti.common.repository.CommonRepository;
import fi.vm.yti.common.util.MapperUtils;
import fi.vm.yti.security.AuthenticatedUserProvider;
import fi.vm.yti.security.AuthorizationException;
import fi.vm.yti.security.YtiUser;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static fi.vm.yti.common.mapper.OrganizationMapper.mapOrganizationsToModel;

@Service
public class GroupManagementService {
    private static final Logger LOG = LoggerFactory.getLogger(GroupManagementService.class);

    private final CommonRepository coreRepository;

    private final AuthenticatedUserProvider userProvider;

    private final WebClient webClient;

    @Value("${fake.login.allowed:false}")
    private boolean fakeLoginAllowed;

    private final Cache<String, GroupManagementUserDTO> userCache;

    private static final String PUBLIC_API = "public-api";

    private static final String PRIVATE_API = "private-api";

    public GroupManagementService(
            @Qualifier("groupManagementClient") WebClient webClient,
            CommonRepository coreRepository,
            AuthenticatedUserProvider userProvider) {
        this.webClient = webClient;
        this.coreRepository = coreRepository;
        this.userProvider = userProvider;
        userCache = CacheBuilder.newBuilder().build();
    }

    public void initOrganizations() {
        var organizations = fetchOrganizations(true);
        if (organizations == null || organizations.isEmpty()) {
            throw new GroupManagementException("No organizations found, is group management service down?");
        }

        var model = ModelFactory.createDefaultModel();
        mapOrganizationsToModel(organizations, model);
        coreRepository.put(Constants.ORGANIZATION_GRAPH, model);
        LOG.info("Initialized organizations with {} organizations", organizations.size());
    }

    @Scheduled(fixedRateString = "${groupmanagement.syncInterval.organizations:30}", timeUnit = TimeUnit.MINUTES)
    public void updateOrganizations() {
        LOG.info("Updating organizations cache");
        var organizations = fetchOrganizations(false);

        if (organizations != null && !organizations.isEmpty()) {
            var model = coreRepository.fetch(Constants.ORGANIZATION_GRAPH);
            mapOrganizationsToModel(organizations, model);
            coreRepository.put(Constants.ORGANIZATION_GRAPH, model);
            coreRepository.invalidateOrganizationCache();
            LOG.info("Updated {} organizations to fuseki", organizations.size());
        } else {
            LOG.info("No updates to organizations found");
        }
    }

    public void initUsers() {
        LOG.info("Initializing user cache");
        var users = fetchUsers(false, false);

        if (users == null || users.isEmpty()) {
            throw new GroupManagementException("No users found, is group service down?");
        }

        var map = users.stream().collect(Collectors.toMap(user -> user.getId().toString(), user -> user));
        userCache.invalidateAll();
        userCache.putAll(map);
        LOG.info("Initialized user cache with {} users", map.size());
    }

    @Scheduled(fixedRateString = "${groupmanagement.syncInterval.users:30}", timeUnit = TimeUnit.MINUTES)
    public void updateUsers() {
        LOG.info("Updating user cache");
        var users = fetchUsers(false, true);

        if (users != null && !users.isEmpty()) {
            var oldSize = userCache.size();
            var map = users.stream().collect(Collectors.toMap(user -> user.getId().toString(), user -> user));
            userCache.putAll(map);
            LOG.info("Updated {} users to cache, old count: {}, new count: {}", map.size(), oldSize, userCache.size());
        } else {
            LOG.info("No modifications to users found");
        }
    }

    public Consumer<ResourceCommonInfoDTO> mapUser() {
        return (var dto) -> {
            if (dto.getCreator().getId() == null || dto.getModifier().getId() == null) {
                return;
            }
            var creator = userCache.getIfPresent(dto.getCreator().getId());
            var modifier = userCache.getIfPresent(dto.getModifier().getId());
            if (creator != null) {
                dto.getCreator().setName(creator.getFirstName() + " " + creator.getLastName());
            } else {
                dto.getCreator().setName("");
            }

            if (modifier != null) {
                dto.getModifier().setName(modifier.getFirstName() + " " + modifier.getLastName());
            } else {
                dto.getModifier().setName("");
            }
        };
    }

    public List<UUID> getChildOrganizations(UUID orgId) {
        var orgUrn = Constants.URN_UUID + orgId.toString();
        if (!coreRepository.resourceExistsInGraph(Constants.ORGANIZATION_GRAPH, orgUrn)) {
            LOG.warn("Organization not found {}", orgUrn);
            return new ArrayList<>();
        }
        var model = coreRepository.getOrganizations();

        var resource = model.getResource(orgUrn);

        return MapperUtils.arrayPropertyToList(resource, SuomiMeta.parentOrganization).stream()
                .map(MapperUtils::getUUID).toList();
    }

    public List<GroupManagementUserDTO> getFakeableUsers() {
        if (fakeLoginAllowed) {
            return fetchUsers(true, false);
        }
        return List.of();
    }

    public Set<UUID> getOrganizationsForUser(YtiUser user) {
        final var rolesInOrganizations = user.getRolesInOrganizations();

        var orgIds = new HashSet<>(rolesInOrganizations.keySet());

        // show child organization's incomplete content for main organization users
        var childOrganizationIds = orgIds.stream()
                .map(this::getChildOrganizations)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        orgIds.addAll(childOrganizationIds);
        return orgIds;
    }

    public List<GroupManagementUserRequestDTO> getUserRequests() {
        var user = userProvider.getUser();

        if (user.isAnonymous()) {
            throw new AuthorizationException("User not authenticated");
        }

        return webClient.get().uri(builder -> builder
                        .pathSegment(PRIVATE_API, "requests")
                        .queryParam("userId", user.getId())
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GroupManagementUserRequestDTO>>() {
                }).block();
    }

    public void sendRequest(UUID organizationId, String[] roles) {
        var user = userProvider.getUser();

        if (user.isAnonymous()) {
            throw new AuthorizationException("User not authenticated");
        }

        webClient.post().uri(builder -> builder
                        .pathSegment(PRIVATE_API, "request")
                        .queryParam("userId", user.getId())
                        .queryParam("organizationId", organizationId)
                        .queryParam("role", Arrays.asList(roles))
                        .build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private List<GroupManagementUserDTO> fetchUsers(boolean publicUsers, boolean lastModifiedOnly) {
        String apiPath = publicUsers ? PUBLIC_API : PRIVATE_API;

        var client = webClient.get()
                .uri(builder -> builder
                        .pathSegment(apiPath, "users")
                        .build());
        if (lastModifiedOnly) {
            client.ifModifiedSince(ZonedDateTime.now().minusMinutes(30));
        }

        return client.retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GroupManagementUserDTO>>() {
                })
                .block();
    }

    private List<GroupManagementOrganizationDTO> fetchOrganizations(boolean init) {
        var client = webClient.get().uri(builder -> builder
                .pathSegment(PUBLIC_API, "organizations")
                .queryParam("onlyValid", "true")
                .build());

        if (!init) {
            client.ifModifiedSince(ZonedDateTime.now().minusMinutes(30));
        }

        return client.retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GroupManagementOrganizationDTO>>() {
                })
                .block();
    }

    private static final class GroupManagementException extends RuntimeException {
        private GroupManagementException(String message) {
            super(message);
        }
    }
}
