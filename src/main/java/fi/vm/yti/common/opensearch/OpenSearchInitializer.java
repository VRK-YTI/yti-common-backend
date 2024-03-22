package fi.vm.yti.common.opensearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.sparql.function.library.version;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Consumer;

@Service
public class OpenSearchInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(OpenSearchInitializer.class);

    @Value("${openSearch.initOnStartUp:true}")
    private boolean initIndexsOnStartUp;

    private final OpenSearchClientWrapper client;

    @Autowired
    public OpenSearchInitializer(OpenSearchClientWrapper client) {
        this.client = client;
    }

    public void initIndexes(InitIndexesFunction initFn, Map<String, TypeMapping> mappings) {
        if (!initIndexsOnStartUp) {
            LOG.info("Index initialization is disabled on startup. Please set openSearch.initOnStartUp=true to reindex");
            return;
        }

        try {
            var indexes = new ArrayList<String>();
            mappings.keySet().forEach(indexes::add);

            LOG.info("Init indexes {}", String.join(", ", indexes));

            for (var index : mappings.keySet()) {
                client.cleanIndexes(index);
                LOG.info("Removed index {}", index);

                client.createIndex(index, mappings.get(index));
                LOG.info("Created index {}", index);
            }

            initFn.apply();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @FunctionalInterface
    public interface InitIndexesFunction {
        void apply();
    }
}
