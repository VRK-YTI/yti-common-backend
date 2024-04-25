package fi.vm.yti.common.service;

import fi.vm.yti.common.dto.MetaDataDTO;
import fi.vm.yti.common.dto.MetaDataInfoDTO;
import fi.vm.yti.common.repository.BaseRepository;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractGraphService<R extends BaseRepository> {

    private final R repository;

    protected AbstractGraphService(R repository) {
        this.repository = repository;
    }

    public abstract MetaDataInfoDTO get(String prefix);

    public abstract MetaDataInfoDTO get(String prefix, String version);

    public abstract URI create(MetaDataDTO dto) throws URISyntaxException;

    public abstract void update(String prefix, MetaDataDTO dto);

    public abstract void delete(String prefix);

    public abstract void delete(String prefix, String version);

    public boolean exists(String graphURI) {
        return repository.graphExists(graphURI);
    }
}
