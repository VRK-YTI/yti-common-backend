package fi.vm.yti.common.service;

import fi.vm.yti.common.Constants;
import fi.vm.yti.common.MapperTestUtils;
import fi.vm.yti.common.dto.GroupManagementUserDTO;
import fi.vm.yti.common.dto.ResourceInfoBaseDTO;
import fi.vm.yti.common.dto.UserDTO;
import fi.vm.yti.common.properties.SuomiMeta;
import fi.vm.yti.common.repository.CommonRepository;

import fi.vm.yti.security.Role;
import fi.vm.yti.security.YtiUser;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Import({
        GroupManagementService.class,
})
class GroupManagementServiceTest {

    @MockBean
    @Qualifier("groupManagementClient")
    private WebClient client;

    @MockBean
    private CommonRepository coreRepository;

    @Autowired
    GroupManagementService groupManagementService;

    private final UserDTO creatorUser = new UserDTO(UUID.randomUUID().toString());
    private final UserDTO modifierUser = new UserDTO(UUID.randomUUID().toString());

    @Test
    void testMapUserToCommonDTO() {
        var common = new ResourceInfoBaseDTO();
        common.setCreator(creatorUser);
        common.setModifier(modifierUser);

        var creatorResult = new GroupManagementUserDTO(UUID.fromString(creatorUser.getId()),
                "test@localhost",
                "Test",
                "Creator",
                null);

        var modifierResult =new GroupManagementUserDTO(UUID.fromString(modifierUser.getId()),
                "test@localhost",
                "Test",
                "Modifier",
                null);

        MapperTestUtils.mockWebClient(client, List.of(creatorResult, modifierResult));

        groupManagementService.initUsers();
        groupManagementService.mapUser().accept(common);

        assertEquals("Test Creator", common.getCreator().getName());
        assertEquals("Test Modifier", common.getModifier().getName());
    }

    @Test
    void testGetChildOrganizations() {
        var model = ModelFactory.createDefaultModel();
        var orgId = UUID.randomUUID();
        var parentId = UUID.randomUUID();
        var organizationURN = Constants.URN_UUID + orgId;
        var parentURN = Constants.URN_UUID + parentId;

        model.createResource(organizationURN)
                .addProperty(SuomiMeta.parentOrganization, ResourceFactory.createResource(parentURN));

        when(coreRepository.resourceExistsInGraph(eq(Constants.ORGANIZATION_GRAPH),
                Mockito.anyString())).thenReturn(true);
        when(coreRepository.getOrganizations()).thenReturn(model);

        var result = groupManagementService.getChildOrganizations(orgId);

        assertEquals(parentId, result.get(0));
    }

    @Test
    void testGetOrganizationsForUser() {
        var orgId = UUID.randomUUID();
        var parentId = UUID.randomUUID();

        var model = ModelFactory.createDefaultModel();
        model.createResource(Constants.URN_UUID + orgId)
                .addProperty(SuomiMeta.parentOrganization,
                        ResourceFactory.createResource(Constants.URN_UUID + parentId));

        when(coreRepository.resourceExistsInGraph(eq(Constants.ORGANIZATION_GRAPH),
                Mockito.anyString())).thenReturn(true);
        when(coreRepository.getOrganizations()).thenReturn(model);

        var user = new YtiUser("test@localhost",
                "test",
                "tester",
                UUID.randomUUID(),
                false,
                false,
                LocalDateTime.of(2001, 1, 1, 0,0),
                LocalDateTime.of(2001, 1, 1, 0,0),
                new HashMap<>(Map.of(orgId, Set.of(Role.DATA_MODEL_EDITOR))),
                "",
                "");

        assertEquals(Set.of(orgId, parentId), groupManagementService.getOrganizationsForUser(user));
    }


}
