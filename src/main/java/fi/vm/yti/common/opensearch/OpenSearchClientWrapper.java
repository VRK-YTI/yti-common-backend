package fi.vm.yti.common.opensearch;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.opensearch._types.Refresh;
import org.opensearch.client.opensearch._types.analysis.*;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.UpdateRequest;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.opensearch.client.opensearch.indices.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import fi.vm.yti.common.exception.OpenSearchExceptionWrapper;
import fi.vm.yti.common.util.CommonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static fi.vm.yti.common.opensearch.OpenSearchUtil.logPayload;

@Service
public class OpenSearchClientWrapper {

    private static final Logger logger = LoggerFactory.getLogger(OpenSearchClientWrapper.class);

    @Value("${openSearch.bulkMaxSize:500}")
    private Integer bulkMaxSize;

    private final OpenSearchClient client;

    @Autowired
    public OpenSearchClientWrapper(final OpenSearchClient client) {
        this.client = client;
    }

    public boolean indexExists(String index) throws IOException {
        return client.indices().exists(
                new ExistsRequest.Builder().index(index).build()
        ).value();
    }

    /**
     * Delete an index if it exists.
     *
     * @param indexes index name
     * @throws IOException in case there is a problem sending the request or parsing back the response
     */
    public void cleanIndexes(String... indexes) throws IOException {

        for (String index : indexes) {
            boolean exists = indexExists(index);
            if (exists) {
                logger.info("Cleaning index: {}", index);
                this.client.indices().delete(new DeleteIndexRequest.Builder()
                .index(index).build());
            }
        }
    }

    public void createIndex(String index, TypeMapping mappings) {

        var ngram = new Tokenizer.Builder()
                .definition(new TokenizerDefinition.Builder()
                        .ngram(new NGramTokenizer.Builder()
                            .tokenChars(List.of(TokenChar.Letter, TokenChar.Digit))
                            .maxGram(3)
                            .minGram(3)
                            .build())
                    .build())
                .build();

        var edgeNgram = new Tokenizer.Builder()
                .definition(new TokenizerDefinition.Builder()
                        .edgeNgram(new EdgeNGramTokenizer.Builder()
                                .tokenChars(List.of(TokenChar.Letter, TokenChar.Digit))
                                .maxGram(20)
                                .minGram(3)
                                .build())
                        .build())
                .build();

        var edgeNgramAnalyzer = new Analyzer.Builder()
                .custom(new CustomAnalyzer.Builder()
                    .tokenizer("edgeNgram")
                    .filter(List.of("lowercase"))
                    .build())
                .build();

        var ngramAnalyzer = new Analyzer.Builder()
                .custom(new CustomAnalyzer.Builder()
                        .tokenizer("ngram")
                        .filter(List.of("lowercase"))
                        .build())
                .build();

        var request = new CreateIndexRequest.Builder()
                .index(index)
                .mappings(mappings)
                .settings(new IndexSettings.Builder()
                        .analysis(new IndexSettingsAnalysis.Builder()
                                .analyzer("edgeNgramAnalyzer", edgeNgramAnalyzer)
                                .analyzer("ngramAnalyzer", ngramAnalyzer)
                                .tokenizer("edgeNgram", edgeNgram)
                                .tokenizer("ngram", ngram)
                                .build())
                        .build())
                .build();
        logPayload(request, index);
        try {
            client.indices().create(request);
            logger.info("Index {} created", index);
        } catch (IOException | OpenSearchException ex) {
            logger.warn("Index creation failed for " + index, ex);
        }
    }

    public <T extends IndexBase> void putToIndex(String index, T doc) {
        String encId = CommonUtils.encode(doc.getId());
        try {
            IndexRequest<T> indexReq = new IndexRequest.Builder<T>()
                    .index(index)
                    .refresh(Refresh.True)
                    .id(encId)
                    .document(doc)
                    .build();

            logPayload(indexReq, index);
            client.index(indexReq);
            logger.debug("Indexed {} to {}}", doc.getId(), index);
        } catch (IOException | OpenSearchException e) {
            logger.warn("Could not add to index: " + doc.getId(), e);
        }
    }

    public <T extends IndexBase> void updateToIndex(String index, T doc) {
        String encId = CommonUtils.encode(doc.getId());
        try {
            var request = new UpdateRequest.Builder<String, T>()
                    .index(index)
                    .refresh(Refresh.True)
                    .id(encId)
                    .doc(doc)
                    .build();
            logPayload(request, index);
            client.update(request, String.class);
            logger.debug("Updated {} to {}", doc.getId(), index);
        } catch (IOException | OpenSearchException e) {
            logger.warn("Could not update to index: " + doc.getId(), e);
        }
    }

        public <T extends IndexBase> void bulkInsert(String indexName,
                                                 List<T> documents) {
        List<BulkOperation> bulkOperations = new ArrayList<>();
        documents.forEach(doc ->
                bulkOperations.add(new IndexOperation.Builder<IndexBase>()
                        .index(indexName)
                        .id(CommonUtils.encode(doc.getId()))
                        .document(doc)
                        .build().
                        _toBulkOperation())
        );
        if (bulkOperations.isEmpty()) {
            logger.info("No data to index");
            return;
        }

        Iterables.partition(bulkOperations, bulkMaxSize).forEach(batch -> {
            var bulkRequest = new BulkRequest.Builder()
                    .operations(batch);
            try {
                var response = client.bulk(bulkRequest.build());

                if (response.errors()) {
                    logger.warn("Errors occurred in bulk operation");
                    response.items().stream()
                            .filter(i -> i.error() != null)
                            .forEach(i -> logger.warn("Error in document {}, caused by {}", i.id(), i.error().reason()));
                }
                logger.debug("Bulk insert status for {}: errors: {}, items: {}, took: {}ms",
                        indexName, response.errors(), response.items().size(), response.took());
            } catch (IOException e) {
                logger.warn("Error in bulk operation", e);
            }
        });
    }

    public void removeFromIndexWithQuery(String index, Query query) {
        try {
            final long startTime = System.currentTimeMillis();
            DeleteByQueryRequest req = new DeleteByQueryRequest.Builder()
                    .index(index)
                    .query(query)
                    .refresh(true)
                    .build();
            var response = client.deleteByQuery(req);
            logger.info("Removed {} items from {} (took {} ms)", response.deleted(), index, System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public void removeFromIndex(String index, String id) {
        String encId = CommonUtils.encode(id);
        try {
            final long startTime = System.currentTimeMillis();
            DeleteRequest req = new DeleteRequest.Builder()
                    .index(index)
                    .id(encId)
                    .refresh(Refresh.WaitFor)
                    .build();
            client.delete(req);
            logger.info("Removed {} from {} (took {} ms)", id, index, System.currentTimeMillis() - startTime);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public <T extends IndexBase> SearchResponse<T> searchResponse(SearchRequest request, Class<T> type) {
        try {
            return client.search(request, type);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new OpenSearchExceptionWrapper(e.getMessage(), String.join(", ", request.index()));
        }
    }

    public <T extends IndexBase> SearchResponseDTO<T> search(SearchRequest request, Class<T> type) {
        var response = new SearchResponseDTO<T>();
        
        try {
            var result = client.search(request, type);
            response.setTotalHitCount(result.hits().total().value());
            var sources = result.hits().hits().stream()
                    .filter(hit -> hit.source() != null)
                    .map(hit -> {
                        var base = hit.source();
                        base.setHighlights(hit.highlight());
                        return base;
                    })
                    .toList();
            response.setResponseObjects(sources);
            response.setPageFrom(request.from());
            response.setPageSize(request.size());
            return response;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new OpenSearchExceptionWrapper(e.getMessage(), String.join(", ", request.index()));
        }
    }

}