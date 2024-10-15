package fi.vm.yti.common.opensearch;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import jakarta.json.stream.JsonGenerator;
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

    /**
     * Serialize object to JSON
     * @param object object to serialize
     * @return object as JSON string
     */
    public static String getPayload(JsonpSerializable object) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator generator = MAPPER.jsonProvider().createGenerator(out);
        MAPPER.serialize(object, generator);
        generator.close();
        return out.toString();
    }

    public static Map<String, DynamicTemplate> getDynamicTemplate(String name, String pathMatch) {
        return Map.of(name, new DynamicTemplate.Builder()
                .pathMatch(pathMatch)
                .mapping(getTextProperty("default")).build());
    }

    public static Map<String, DynamicTemplate> getDynamicTemplate(String name, String pathMatch, String analyzer) {
        return Map.of(name, new DynamicTemplate.Builder()
                .pathMatch(pathMatch)
                .mapping(getTextProperty(analyzer)).build());
    }

    public static Map<String, DynamicTemplate> getDynamicTemplateWithSortKey(String name, String pathMatch) {
        return Map.of(name, new DynamicTemplate.Builder()
                .pathMatch(pathMatch)
                .mapping(getSortedKeyWordProperty()).build());
    }

    public static Property getKeywordProperty() {
        return new Property.Builder()
                .keyword(new KeywordProperty.Builder().build())
                .build();
    }

    public static Property getTextProperty() {
        return getTextProperty(null);
    }

    private static Property getTextProperty(String analyzer) {
        if (analyzer == null) {
            analyzer = "default";
        }
        return new Property.Builder()
                .text(new TextProperty.Builder()
                        .analyzer(analyzer)
                        .build())
                .build();
    }

    public static Property getSortedKeyWordProperty() {
        return getSortedKeyWordProperty(null);
    }

    public static Property getSortedKeyWordProperty(String analyzer) {
        if (analyzer == null) {
            analyzer = "default";
        }
        return new Property.Builder()
                .text(new TextProperty.Builder()
                        .analyzer(analyzer)
                        .fields("sortKey",
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

    public static Map<String, Property> getMetaDataProperties() {
        return Map.ofEntries(
                Map.entry("id", getKeywordProperty()),
                Map.entry("uri", getKeywordProperty()),
                Map.entry("status", getKeywordProperty()),
                Map.entry("type", getKeywordProperty()),
                Map.entry("prefix", getKeywordProperty()),
                Map.entry("organizations", getKeywordProperty()),
                Map.entry("languages", getKeywordProperty()),
                Map.entry("groups", getKeywordProperty()),
                Map.entry("created", getDateProperty()),
                Map.entry("contentModified", getDateProperty())
        );
    }

    public static List<Map<String, DynamicTemplate>> getMetaDataDynamicTemplates() {
        return List.of(
                getDynamicTemplateWithSortKey("label", "label.*"),
                getDynamicTemplate("description", "description.*")
        );
    }
}
