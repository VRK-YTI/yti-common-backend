package fi.vm.yti.common.util;

import fi.vm.yti.common.exception.MappingError;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MapperUtilsTest {

    @Test
    void testGetUUIDFromURN() {
        var uuid = UUID.randomUUID();
        var result = MapperUtils.getUUID("urn:uuid:" + uuid);
        assertEquals(uuid, result);
    }

    @Test
    void testLocalizedPropertyToMap() {
        var model = ModelFactory.createDefaultModel();
        var resource = model.createResource("http://test-resource")
                .addProperty(RDFS.label, model.createLiteral("test 1", "en"))
                .addProperty(RDFS.label, model.createLiteral("test 2", "fi"));

        assertEquals(Map.of("en", "test 1", "fi", "test 2"),
                MapperUtils.localizedPropertyToMap(resource, RDFS.label));
        assertEquals(Map.of(), MapperUtils.localizedPropertyToMap(resource, RDFS.comment));
    }

    @Test
    void testAddLocalizedProperty() {
        var model = ModelFactory.createDefaultModel();
        var resource = model.createResource("http://test-resource");
        var languages = Set.of("fi", "en");

        MapperUtils.addLocalizedProperty(languages, Map.of("fi", "test"), resource, RDFS.label);

        var label = resource.getProperty(RDFS.label);
        assertEquals("fi", label.getLanguage());
        assertEquals("test", label.getString());

        MapperUtils.addLocalizedProperty(languages, null, resource, RDFS.comment);
        MapperUtils.addLocalizedProperty(languages, Map.of(), resource, RDFS.comment);
        assertFalse(resource.hasProperty(RDFS.comment));

        var invalidLanguages = Map.of("fr", "test");
        assertThrows(MappingError.class, () ->
            MapperUtils.addLocalizedProperty(languages, invalidLanguages, resource, RDFS.label)
        );
    }

    @Test
    void testPropertyToString() {
        var model = ModelFactory.createDefaultModel();
        var resource = model.createResource("http://test-resource")
                .addProperty(RDFS.label, "test");

        assertEquals("test", MapperUtils.propertyToString(resource, RDFS.label));
        assertNull(MapperUtils.propertyToString(resource, RDFS.comment));
    }

    @Test
    void testArrayPropertyToList() {
        var model = ModelFactory.createDefaultModel();
        var resource = model.createResource("https://test-resource")
                .addProperty(RDFS.subClassOf, "sub-class-1")
                .addProperty(RDFS.subClassOf, "sub-class-2")
                .addProperty(RDFS.subClassOf, "sub-class-3");

        assertTrue(List.of("sub-class-1", "sub-class-2", "sub-class-3")
                .containsAll(MapperUtils.arrayPropertyToList(resource, RDFS.subClassOf)));
    }
}
