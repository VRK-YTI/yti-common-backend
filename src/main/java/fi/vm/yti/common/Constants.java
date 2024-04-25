package fi.vm.yti.common;

import java.util.List;
import java.util.Map;

public class Constants {

    private Constants(){
        // utility class
    }
    public static final String URN_UUID = "urn:uuid:";
    public static final String RESOURCE_SEPARATOR = "/";
    public static final List<String> USED_LANGUAGES = List.of("fi", "sv", "en");
    public static final String ORGANIZATION_GRAPH = "urn:yti:organizations";
    public static final String SERVICE_CATEGORY_GRAPH = "urn:yti:servicecategories";
    public static final String DEFAULT_LANGUAGE = "fi";
    public static final String DATA_MODEL_NAMESPACE = "https://iri.suomi.fi/model/";
    public static final String TERMINOLOGY_NAMESPACE = "https://iri.suomi.fi/terminology/";

    public static final Map<String, String> PREFIXES = Map.of(
            "rdfs", "http://www.w3.org/2000/01/rdf-schema#",
            "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            "dcterms", "http://purl.org/dc/terms/",
            "owl", "http://www.w3.org/2002/07/owl#",
            "dcap", "http://purl.org/ws-mmi-dc/terms/",
            "xsd", "http://www.w3.org/2001/XMLSchema#",
            "suomi-meta", "https://iri.suomi.fi/model/suomi-meta/",
            "skos", "http://www.w3.org/2004/02/skos/core#",
            "sh", "http://www.w3.org/ns/shacl#"
    );
}
