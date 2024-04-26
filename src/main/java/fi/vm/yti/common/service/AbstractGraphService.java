package fi.vm.yti.common.service;

import fi.vm.yti.common.enums.GraphType;
import fi.vm.yti.common.repository.BaseRepository;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractGraphService<M, T, R extends BaseRepository> {

    private final R repository;

    protected AbstractGraphService(R repository) {
        this.repository = repository;
    }

    public abstract M get(String prefix);

    public abstract M get(String prefix, String version);

    public abstract URI create(T dto, GraphType graphType) throws URISyntaxException;

    public abstract void update(String prefix, T dto);

    public abstract void delete(String prefix);

    public abstract void delete(String prefix, String version);

    public boolean exists(String graphURI) {
        return repository.graphExists(graphURI);
    }
}
