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

    public static final Map<String, String> PREFIXES = Map.ofEntries(
            Map.entry("rdfs", "http://www.w3.org/2000/01/rdf-schema#"),
            Map.entry("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
            Map.entry("dcterms", "http://purl.org/dc/terms/"),
            Map.entry("owl", "http://www.w3.org/2002/07/owl#"),
            Map.entry("dcap", "http://purl.org/ws-mmi-dc/terms/"),
            Map.entry("xsd", "http://www.w3.org/2001/XMLSchema#"),
            Map.entry("suomi-meta", "https://iri.suomi.fi/model/suomi-meta/"),
            Map.entry("skos", "http://www.w3.org/2004/02/skos/core#"),
            Map.entry("skos-xl", "http://www.w3.org/2008/05/skos-xl#"),
            Map.entry("sh", "http://www.w3.org/ns/shacl#"),
            Map.entry("foaf", "http://xmlns.com/foaf/0.1/")
    );
}
