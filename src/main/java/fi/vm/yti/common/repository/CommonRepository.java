package fi.vm.yti.common.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fi.vm.yti.common.Constants;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class CommonRepository extends BaseRepository {

    private final Logger logger = LoggerFactory.getLogger(CommonRepository.class);

    private final Cache<String, Model> modelCache;

    public CommonRepository(@Value(("${fuseki.url}")) String endpoint,
                            @Value("${fuseki.cache.common.expiration:1800}") Long cacheExpireTime) {
        super(RDFConnection.connect(endpoint + "/core/get"),
                RDFConnection.connect(endpoint + "/core/data"),
                RDFConnection.connect(endpoint + "/core/sparql"),
                RDFConnection.connect(endpoint + "/core/update"));

        this.modelCache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheExpireTime, TimeUnit.SECONDS)
                .maximumSize(1000)
                .build();
    }

    public void initServiceCategories() {
        var model = RDFDataMgr.loadModel("ptvl-skos.rdf");
        put(Constants.SERVICE_CATEGORY_GRAPH, model);
    }

    public Model getOrganizations() {
        var organizations = modelCache.getIfPresent("organizations");

        if (organizations != null) {
            return organizations;
        }

        organizations = fetch(Constants.ORGANIZATION_GRAPH);
        logger.info("Fetched organizations from fuseki to cache");
        modelCache.put("organizations", organizations);
        return organizations;
    }

    public void invalidateOrganizationCache() {
        modelCache.invalidate("organizations");
    }

    public Model getServiceCategories() {
        var serviceCategories = modelCache.getIfPresent("serviceCategories");

        if (serviceCategories != null) {
            return serviceCategories;
        }

        var cat = "?category";
        ConstructBuilder builder = new ConstructBuilder()
                .addPrefixes(Constants.PREFIXES)
                .addConstruct(cat, RDFS.label, "?label")
                .addConstruct(cat, RDF.type, FOAF.Group)
                .addConstruct(cat, SKOS.notation, "?id")
                .addConstruct(cat, SKOS.note, "?note")
                .addWhere(cat, RDF.type, SKOS.Concept)
                .addWhere(cat, SKOS.prefLabel, "?label")
                .addWhere(cat, SKOS.notation, "?id")
                .addWhere(cat, SKOS.note, "?note")
                .addFilter(new ExprFactory().notexists(
                        new WhereBuilder().addWhere(cat, SKOS.broader, "?topCategory")
                ));

        serviceCategories = queryConstruct(builder.build());
        logger.info("Fetched service categories from fuseki to cache");
        modelCache.put("serviceCategories", serviceCategories);
        return serviceCategories;
    }

}
