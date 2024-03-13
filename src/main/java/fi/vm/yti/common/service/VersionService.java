package fi.vm.yti.common.service;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fi.vm.yti.common.repository.CommonRepository;

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
        var version = versionModel.getResource(versionGraph).getRequiredProperty(OWL.versionInfo).getInt();
        return version;
    }

    public void setVersionNumber(int version) {
        var versionModel = ModelFactory.createDefaultModel()
            .addLiteral(ResourceFactory.createResource(versionGraph), OWL.versionInfo, version);
        commonRepository.put(versionGraph, versionModel);
    }

    public boolean isVersionGraphInitialized() {
        var exists = commonRepository.graphExists(versionGraph);
        return exists;
    }
}
