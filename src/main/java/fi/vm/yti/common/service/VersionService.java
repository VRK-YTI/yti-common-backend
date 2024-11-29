package fi.vm.yti.common.service;

import fi.vm.yti.common.repository.CommonRepository;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VersionService {
    
    private final CommonRepository commonRepository;

    @Value("${version.graph}")
    private String versionGraph;

    public VersionService(CommonRepository coreRepository) {
        this.commonRepository = coreRepository;
    }

    public int getVersionNumber() {
        var versionModel = commonRepository.fetch(versionGraph);
        return versionModel.getResource(versionGraph).getRequiredProperty(OWL.versionInfo).getInt();
    }

    public void setVersionNumber(int version) {
        var versionModel = ModelFactory.createDefaultModel()
            .addLiteral(ResourceFactory.createResource(versionGraph), OWL.versionInfo, version);
        commonRepository.put(versionGraph, versionModel);
    }

    public boolean isVersionGraphInitialized() {
        return commonRepository.graphExists(versionGraph);
    }
}
