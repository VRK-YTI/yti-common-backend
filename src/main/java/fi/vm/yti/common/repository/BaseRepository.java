package fi.vm.yti.common.repository;

import fi.vm.yti.common.exception.JenaQueryException;
import fi.vm.yti.common.exception.ResourceNotFoundException;
import fi.vm.yti.common.util.GraphURI;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.DCTerms;
import org.springframework.http.HttpStatus;

import java.util.function.Consumer;

public abstract class BaseRepository {

    RDFConnection read;
    RDFConnection write;
    RDFConnection sparql;
    RDFConnection update;

    protected BaseRepository(RDFConnection read, RDFConnection write) {
        this.read = read;
        this.write = write;
    }

    protected BaseRepository(RDFConnection read, RDFConnection write, RDFConnection sparql) {
        this.read = read;
        this.write = write;
        this.sparql = sparql;
    }

    protected BaseRepository(RDFConnection read, RDFConnection write, RDFConnection sparql, RDFConnection update) {
        this.read = read;
        this.write = write;
        this.sparql = sparql;
        this.update = update;
    }

    public Model fetch(String graph) {
        try {
            return read.fetch(graph);
        } catch (HttpException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                throw new ResourceNotFoundException(graph);
            } else {
                throw new JenaQueryException();
            }
        }
    }

    public void put(String graph, Model model) {
        write.put(graph, model);
    }

    public void delete(String graph) {
        try {
            write.delete(graph);
        } catch (HttpException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                throw new ResourceNotFoundException(graph);
            } else {
                throw new JenaQueryException();
            }
        }
    }

    public void deleteResource(GraphURI uri) {
        deleteResource(uri.getResourceURI());
    }

    public void deleteResource(String resourceURI) {
        var uri = NodeFactory.createURI(resourceURI);
        var graph = NodeFactory.createURI(uri.getNameSpace());

        var deleteBuilder = new UpdateBuilder();
        var expr = deleteBuilder.getExprFactory();
        var filter = expr.or(
                expr.eq("?s", NodeFactory.createURI(resourceURI)),
                expr.eq("?o", NodeFactory.createURI(resourceURI))
        );
        deleteBuilder.addDelete(graph, "?s", "?p", "?o")
                .addGraph(graph, new WhereBuilder()
                        .addWhere("?s", "?p", "?o")
                        .addFilter(filter));
        try {
            update.update(deleteBuilder.buildRequest());
        } catch (HttpException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                throw new ResourceNotFoundException(resourceURI);
            } else {
                throw new JenaQueryException();
            }
        }
    }

    public boolean graphExists(String graph) {
        var askBuilder = new AskBuilder()
                .addGraph(NodeFactory.createURI(graph), "?s", "?p", "?o");
        return this.queryAsk(askBuilder.build());
    }

    public boolean resourceExistsInGraph(String graph, String resource) {
        return resourceExistsInGraph(graph, resource, true);
    }

    public boolean resourceExistsInGraph(String graph, String resource, boolean caseSensitive) {
        var askBuilder = new AskBuilder();
        if (caseSensitive) {
            askBuilder.addGraph(NodeFactory.createURI(graph),
                    NodeFactory.createURI(resource), "?p", "?o");
        } else {
            var localName = NodeFactory.createURI(resource).getLocalName();
            var expr = askBuilder.getExprFactory();
            askBuilder
                    .addGraph(NodeFactory.createURI(graph), "?s", DCTerms.identifier, "?o")
                    .addFilter(expr.regex("?o", String.format("^%s$", localName), "i"));
        }
        return this.queryAsk(askBuilder.build());
    }

    public Model queryConstruct(Query query) {
        return sparql.queryConstruct(query);
    }

    public void querySelect(Query query, Consumer<QuerySolution> consumer) {
        sparql.querySelect(query, consumer);
    }

    public void querySelect(String query, Consumer<QuerySolution> consumer) {
        sparql.querySelect(query, consumer);
    }

    public boolean queryAsk(Query query) {
        try {
            return sparql.queryAsk(query);
        } catch (HttpException ex) {
            throw new JenaQueryException();
        }
    }

    public void queryUpdate(String query) {
        update.update(query);
    }

    public void queryUpdate(UpdateRequest query) {
        update.update(query);
    }

    public boolean isHealthy() {
        var query = new AskBuilder()
                .addWhere("?s", "?p", "?o")
                .setLimit(1)
                .build();
        this.queryAsk(query); // may return true or false, depending on if there is any data

        // If the queryAsk method does not throw an exception, the connection is healthy
        return true;
    }
}
