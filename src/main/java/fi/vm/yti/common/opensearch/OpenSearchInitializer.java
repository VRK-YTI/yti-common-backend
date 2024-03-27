package fi.vm.yti.common.opensearch;

import java.util.Map;

import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OpenSearchInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(OpenSearchInitializer.class);

    @Value("${openSearch.initOnStartUp:true}")
    private boolean initIndexesOnStartUp;

    private final OpenSearchClientWrapper client;

    @Autowired
    public OpenSearchInitializer(OpenSearchClientWrapper client) {
        this.client = client;
    }

    public void initIndexes(InitIndexesFunction initFn, Map<String, TypeMapping> mappings) {
        if (!initIndexesOnStartUp) {
            LOG.info("Index initialization is disabled on startup. Please set openSearch.initOnStartUp=true to reindex");
            return;
        }

        try {
            for (var mapping : mappings.entrySet()) {
                var index = mapping.getKey();
                LOG.info("Init index {}", index);
                client.cleanIndexes(index);
                client.createIndex(index, mapping.getValue());
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
