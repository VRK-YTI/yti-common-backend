package fi.vm.yti.common.util;

import fi.vm.yti.common.properties.DCAP;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.vocabulary.OWL;

public class ModelWrapper extends ModelCom {

    private final Model model;
    private final String graphURI;
    private final Resource modelResource;
    private final String version;
    private final String prefix;

    public ModelWrapper(Model model, String graph) {
        super(model.getGraph());

        this.model = model;
        this.graphURI = graph;

        var modelSubj = model.listStatements(null, DCAP.preferredXMLNamespacePrefix, (RDFNode) null)
                .filterKeep(s -> s.getSubject().getURI().startsWith("https://iri.suomi.fi/"));

        if (modelSubj.hasNext()) {
            this.modelResource = model.getResource(modelSubj.next().getSubject().getURI());
            this.version = MapperUtils.propertyToString(this.modelResource, OWL.versionInfo);
            this.prefix = MapperUtils.propertyToString(this.modelResource, DCAP.preferredXMLNamespacePrefix);
        } else {
            this.modelResource = model.getResource(graph);
            this.version = null;
            this.prefix = null;
        }
    }

    public Resource getResourceById(String identifier) {
        return super.getResource(graphURI + identifier);
    }

    public Resource getModelResource() {
        return this.modelResource;
    }

    public String getVersion() {
        return this.version;
    }

    public String getGraphURI() {
        return this.graphURI;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isLibrary() {
        return MapperUtils.isLibrary(this.modelResource);
    }

    public boolean isProfile() {
        return MapperUtils.isApplicationProfile(this.modelResource);
    }

    public boolean isTerminology() {
        return MapperUtils.isTerminology(this.modelResource);
    }
}
