package fi.vm.yti.common.mapper;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

import fi.vm.yti.common.dto.BaseDTO;
import fi.vm.yti.common.dto.ResourceCommonDTO;
import fi.vm.yti.common.dto.UserDTO;
import fi.vm.yti.common.exception.MappingError;
import fi.vm.yti.common.properties.SuomiMeta;
import fi.vm.yti.common.util.GraphURI;
import fi.vm.yti.security.YtiUser;

import java.util.*;
import java.util.function.Consumer;

public class MapperUtils {

    private MapperUtils() {
        // Utility class
    }

    /**
     * Get UUID from urn
     * Will return null if urn cannot be parsed
     * @param urn URN string formatted as urn:uuid:{uuid}
     * @return UUID
     */
    public static UUID getUUID(String urn) {
        try {
            return UUID.fromString(urn.replace("urn:uuid:", ""));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Localized property to Map of (language, value). If no language specified for property
     * (e.g. external classes), handle that value as an english content
     * @param resource Resource to get property from
     * @param property Property type
     * @return Map of (language, value)
     */
    public static Map<String, String> localizedPropertyToMap(Resource resource, Property property){
        var map = new HashMap<String, String>();
        resource.listProperties(property).forEach(prop -> {
            var lang = prop.getLanguage();
            var value = prop.getString();
            if (lang == null || lang.isBlank()) {
                map.put("en", value);
            } else {
                map.put(lang, value);
            }
        });
        return map;
    }

    /**
     * Convert property to String, with null checks to ensure no NullPointerException
     * @param resource Resource to get property from
     * @param property Property
     * @return String if property is found, null if not
     */
    public static String propertyToString(Resource resource, Property property){
        var prop = resource.getProperty(property);
        //null check for property
        if(prop == null){
            return null;
        }
        var object = prop.getObject();
        //null check for object
        return object == null ? null : object.toString();
    }

    /**
     * Convert array property to list of strings
     * @param resource Resource to get property from
     * @param property Property type
     * @return List of property values
     */
    public static List<String> arrayPropertyToList(Resource resource, Property property){
        var list = new ArrayList<String>();
        try{
            var statement = resource.listProperties(property)
                    .filterDrop(p -> p.getObject().isAnon())
                    .toList();
            if (statement.isEmpty()) {
                return list;
            }
            statement.get(0)
                    .getList()
                    .asJavaList()
                    .forEach(node -> list.add(node.toString()));
        }catch(JenaException ex){
            //if item could not be gotten as list it means it is multiple statements of the property
            resource.listProperties(property)
                    .filterDrop(p -> p.getObject().isAnon())
                    .forEach(val -> list.add(val.getObject().toString()));
        }
        return list;
    }

    /**
     * Convert array property to set of strings
     * @param resource Resource to get property from
     * @param property Property type
     * @return Set of property values, empty if property is not found
     */
    public static Set<String> arrayPropertyToSet(Resource resource, Property property){
        return new HashSet<>(arrayPropertyToList(resource, property));
    }

   /**
     * Add localized property to Jena model
     * @param data Map of (language, value)
     * @param resource Resource to add to
     * @param property Property to add
     * @param model Jena model to add to
     */
    public static void addLocalizedProperty(Set<String> languages,
                                            Map<String, String> data,
                                            Resource resource,
                                            Property property,
                                            Model model) {
        if (data == null || languages == null || languages.isEmpty()) {
            return;
        }

        data.forEach((lang, value) -> {
            if(!languages.contains(lang)){
                throw new MappingError("Model missing language for localized property {" + lang + "}");
            }
            resource.addProperty(property, model.createLiteral(value, lang));
        });
    }

    /**
     * Adds an optional string property
     * This has a null check, so it does not need to be separately added
     * @param resource Resource
     * @param property Property
     * @param value Value
     */
    public static void addOptionalStringProperty(Resource resource, Property property, String value){
        if(value != null && !value.isBlank()){
            resource.addProperty(property, value);
        }
    }

    public static void addOptionalUriProperty(Resource resource, Property property, String value){
        if(value != null && !value.isBlank()){
            resource.addProperty(property, ResourceFactory.createResource(value));
        }
    }


    public static void addCreationMetadata(Resource resource, YtiUser user) {
        var creationDate = new XSDDateTime(Calendar.getInstance());
        resource.addProperty(DCTerms.modified, ResourceFactory.createTypedLiteral(creationDate))
                .addProperty(DCTerms.created, ResourceFactory.createTypedLiteral(creationDate))
                .addProperty(SuomiMeta.creator, user.getId().toString())
                .addProperty(SuomiMeta.modifier, user.getId().toString());
    }

    public static void addUpdateMetadata(Resource resource, YtiUser user) {
        var updateDate = new XSDDateTime(Calendar.getInstance());
        resource.removeAll(DCTerms.modified);
        resource.addProperty(DCTerms.modified, ResourceFactory.createTypedLiteral(updateDate));
        resource.removeAll(SuomiMeta.modifier);
        resource.addProperty(SuomiMeta.modifier, user.getId().toString());
    }

    public static void mapCreationInfo(ResourceCommonDTO dto,
                                        Resource resource,
                                        Consumer<ResourceCommonDTO> userMapper) {
        var created = resource.getProperty(DCTerms.created).getLiteral().getString();
        var modified = resource.getProperty(DCTerms.modified).getLiteral().getString();
        dto.setCreated(created);
        dto.setModified(modified);
        dto.setCreator(new UserDTO(MapperUtils.propertyToString(resource, SuomiMeta.creator)));
        dto.setModifier(new UserDTO(MapperUtils.propertyToString(resource, SuomiMeta.modifier)));

        if (userMapper != null) {
            userMapper.accept(dto);
        }
    }

    public static Resource addCommonResourceInfo(Model model, GraphURI uri, BaseDTO dto) {
        var modelResource = model.getResource(uri.getModelResourceURI());
        var languages = MapperUtils.arrayPropertyToSet(modelResource, DCTerms.language);
        var status = MapperUtils.propertyToString(modelResource, SuomiMeta.publicationStatus);

        var resource = model.createResource(uri.getResourceURI())
                .addProperty(SuomiMeta.publicationStatus, ResourceFactory.createResource(status))
                .addProperty(RDFS.isDefinedBy, modelResource)
                .addProperty(DCTerms.identifier, ResourceFactory.createTypedLiteral(dto.getIdentifier(), XSDDatatype.XSDNCName));
        MapperUtils.addLocalizedProperty(languages, dto.getLabel(), resource, RDFS.label, model);
        MapperUtils.addLocalizedProperty(languages, dto.getNote(), resource, RDFS.comment, model);
        MapperUtils.addOptionalStringProperty(resource, SKOS.editorialNote, dto.getEditorialNote());
        MapperUtils.addOptionalUriProperty(resource, DCTerms.subject, dto.getSubject());
        return resource;
    }
}
