package fi.vm.yti.common.service;

import fi.vm.yti.common.repository.BaseRepository;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractResourceService<O1, O2, R extends BaseRepository> {

    private final R repository;

    protected AbstractResourceService(R repository) {
        this.repository = repository;
    }

    public abstract O1 get(String prefix, String identifier);

    public abstract O1 get(String prefix, String identifier, String version);

    public abstract URI create(String prefix, O2 dto) throws URISyntaxException;

    public abstract void update(String prefix, String identifier, O2 dto);

    public abstract void delete(String prefix, String identifier);

    public abstract void delete(String prefix, String identifier, String version);

    public boolean exists(String graph, String identifier) {
        return repository.resourceExistsInGraph(graph, identifier);
    }
}
