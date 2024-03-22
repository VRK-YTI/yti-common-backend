package fi.vm.yti.common.opensearch;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.JsonpSerializable;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch._types.mapping.DateProperty;
import org.opensearch.client.opensearch._types.mapping.DynamicTemplate;
import org.opensearch.client.opensearch._types.mapping.KeywordProperty;
import org.opensearch.client.opensearch._types.mapping.TextProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opensearch.client.opensearch._types.mapping.*;

public class OpenSearchUtil {

    private static final Logger LOG = LoggerFactory.getLogger(OpenSearchUtil.class);
    private static final JsonpMapper MAPPER = new JacksonJsonpMapper();

    private OpenSearchUtil() {}

    /**
     * Logs payload sent to OpenSearch
     *
     * @param object object
     */
    public static void logPayload(JsonpSerializable object, String index) {
        if (LOG.isDebugEnabled()) {
            var out = new ByteArrayOutputStream();
            var generator = MAPPER.jsonProvider().createGenerator(out);
            MAPPER.serialize(object, generator);
            generator.close();
            LOG.debug("Payload for object of type {} in index {}", object.getClass().getSimpleName(), index);
            LOG.debug(out.toString());
        }
    }


    public static Map<String, DynamicTemplate> getDynamicTemplate(String name, String pathMatch) {
        return Map.of(name, new DynamicTemplate.Builder()
                .pathMatch(pathMatch)
                .mapping(getTextKeyWordProperty()).build());
    }

    public static Property getKeywordProperty() {
        return new Property.Builder()
                .keyword(new KeywordProperty.Builder().build())
                .build();
    }

    public static Property getTextKeyWordProperty() {
        return new Property.Builder()
                .text(new TextProperty.Builder()
                        .fields("keyword",
                                new KeywordProperty.Builder()
                                        .normalizer("lowercase")
                                        .ignoreAbove(256)
                                        .build()
                                        ._toProperty())
                        .build()
                )
                .build();
    }

    public static Property getDateProperty() {
        return new Property.Builder()
                .date(new DateProperty.Builder().build()).build();
    }
}
