package fi.vm.yti.common.util;

import fi.vm.yti.common.properties.DCAP;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelWrapperTest {

    @Test
    void testWrapper() {
        var model = ModelFactory.createDefaultModel();
        var graphURI = "https://iri.suomi.fi/terminology/test/";

        model.createResource(graphURI)
                .addProperty(RDF.type, SKOS.ConceptScheme)
                .addProperty(DCAP.preferredXMLNamespace, graphURI)
                .addProperty(DCAP.preferredXMLNamespacePrefix, "test");

        model.createResource(graphURI + "some-resource")
                .addProperty(RDF.type, SKOS.Concept);

        var wrapper = new ModelWrapper(model, graphURI);
        var modelResource = wrapper.getModelResource();

        assertEquals("test", wrapper.getPrefix());
        assertEquals(graphURI, wrapper.getGraphURI());
        assertEquals(SKOS.ConceptScheme, modelResource.getProperty(RDF.type).getObject());

        var resourceById = wrapper.getResourceById("some-resource");
        assertEquals(SKOS.Concept, resourceById.getProperty(RDF.type).getObject());
    }

    @Test
    void testWrapperWithVersion() {
        var model = ModelFactory.createDefaultModel();
        var graphURI = "https://iri.suomi.fi/model/test/1.0.0/";

        model.createResource("https://iri.suomi.fi/model/test/")
                .addProperty(RDF.type, OWL.Ontology)
                .addProperty(DCAP.preferredXMLNamespace, "https://iri.suomi.fi/model/test/")
                .addProperty(DCAP.preferredXMLNamespacePrefix, "test")
                .addProperty(OWL2.versionInfo, "1.0.0");

        model.createResource(graphURI + "some-resource")
                .addProperty(RDF.type, OWL.Class);

        var wrapper = new ModelWrapper(model, graphURI);

        assertEquals("test", wrapper.getPrefix());
        assertEquals("1.0.0", wrapper.getVersion());
        assertEquals(graphURI, wrapper.getGraphURI());
        assertEquals(OWL.Ontology, wrapper.getModelResource().getProperty(RDF.type).getObject());

        var resourceById = wrapper.getResourceById("some-resource");
        assertEquals("https://iri.suomi.fi/model/test/1.0.0/some-resource", resourceById.getURI());
        assertEquals(OWL.Class, resourceById.getProperty(RDF.type).getObject());
    }
}
