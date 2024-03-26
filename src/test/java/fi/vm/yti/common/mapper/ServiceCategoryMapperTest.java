package fi.vm.yti.common.mapper;

import fi.vm.yti.common.MapperTestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceCategoryMapperTest {

    @Test
    void testMapServiceCategoriesToDTO() {
        var model = MapperTestUtils.getModelFromFile("/service-categories.ttl");
        var serviceCategories = ServiceCategoryMapper.mapToListServiceCategoryDTO(model);

        assertEquals(3, serviceCategories.size());

        var cat = serviceCategories.get(0);

        assertEquals("P11", cat.getIdentifier());
        assertEquals("http://urn.fi/URN:NBN:fi:au:ptvl:v1105", cat.getId());
        assertEquals("Elinkeinot", cat.getLabel().get("fi"));
        assertEquals("Industries", cat.getLabel().get("en"));
        assertEquals("Näringar", cat.getLabel().get("sv"));
    }
}
