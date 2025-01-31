package fi.vm.yti.common.util;

import fi.vm.yti.common.dto.ResourceCommonInfoDTO;
import fi.vm.yti.common.dto.UserDTO;
import fi.vm.yti.common.enums.Status;
import fi.vm.yti.common.exception.MappingError;
import fi.vm.yti.common.properties.SuomiMeta;
import fi.vm.yti.security.YtiUser;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.JenaException;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

import java.util.*;
import java.util.function.Consumer;

public class MapperUtils {

    private MapperUtils() {
        // Utility class
    }

    /**
     * Get UUID from urn
     * Will return null if urn cannot be parsed
     *
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
     *
     * @param resource Resource to get property from
     * @param property Property type
     * @return Map of (language, value)
     */
    public static Map<String, String> localizedPropertyToMap(Resource resource, Property property) {
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
     *
     * @param resource Resource to get property from
     * @param property Property
     * @return String if property is found, null if not
     */
    public static String propertyToString(Resource resource, Property property) {
        var prop = resource.getProperty(property);
        //null check for property
        if (prop == null) {
            return null;
        }
        var object = prop.getObject();

        if (object == null) {
            return null;
        } else if (object.isLiteral()) {
            return object.asLiteral().getString();
        } else {
            return object.toString();
        }
    }

    /**
     * Convert array property to list of strings
     *
     * @param resource Resource to get property from
     * @param property Property type
     * @return List of property values
     */
    public static List<String> arrayPropertyToList(Resource resource, Property property) {
        var list = new ArrayList<String>();
        try {
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
        } catch (JenaException ex) {
            //if item could not be gotten as list it means it is multiple statements of the property
            resource.listProperties(property)
                    .filterDrop(p -> p.getObject().isAnon())
                    .forEach(val -> list.add(val.getObject().toString()));
        }
        return list;
    }

    /**
     * Convert array property to set of strings
     *
     * @param resource Resource to get property from
     * @param property Property type
     * @return Set of property values, empty if property is not found
     */
    public static Set<String> arrayPropertyToSet(Resource resource, Property property) {
        return new HashSet<>(arrayPropertyToList(resource, property));
    }

    /**
     * Add localized property to Jena model
     *
     * @param data     Map of (language, value)
     * @param resource Resource to add to
     * @param property Property to add
     */
    public static void addLocalizedProperty(Set<String> languages,
                                            Map<String, String> data,
                                            Resource resource,
                                            Property property) {
        if (data == null || languages == null || languages.isEmpty()) {
            return;
        }
        data.forEach((lang, value) -> {
            if (!languages.contains(lang)) {
                throw new MappingError("Model missing language for localized property {" + lang + "}");
            }
            resource.addProperty(property, ResourceFactory.createLangLiteral(value, lang));
        });
    }

    /**
     * Adds an optional string property
     * This has a null check, so it does not need to be separately added
     *
     * @param resource Resource
     * @param property Property
     * @param value    Value
     */
    public static void addOptionalStringProperty(Resource resource, Property property, String value) {
        if (value != null && !value.isBlank()) {
            resource.addProperty(property, value);
        }
    }

    public static void addOptionalUriProperty(Resource resource, Property property, String value) {
        if (value != null && !value.isBlank()) {
            resource.addProperty(property, ResourceFactory.createResource(value));
        }
    }

    /**
     * Update Uri property
     * If string is empty|blank value is removed
     * @param resource Resource
     * @param property Property
     * @param value Value
     */
    public static void updateUriProperty(Resource resource, Property property, String value) {
        resource.removeAll(property);
        if (value != null && !value.isBlank()) {
            resource.addProperty(property, ResourceFactory.createResource(value));
        }
    }

    public static <T> T getLiteral(Resource resource, Property property, Class<T> type) {
        var prop = resource.getProperty(property);
        if (prop == null) {
            return null;
        }
        var literal = prop.getLiteral();

        if (type.equals(Integer.class)) {
            return type.cast(literal.getInt());
        } else if (type.equals(Boolean.class)) {
            return type.cast(literal.getBoolean());
        } else if (type.equals(Double.class)) {
            return type.cast(literal.getDouble());
        } else if (type.equals(String.class)) {
            return type.cast(literal.getString());
        }
        return null;
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

    public static void mapCreationInfo(ResourceCommonInfoDTO dto,
                                       Resource resource,
                                       Consumer<ResourceCommonInfoDTO> userMapper) {
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

    /**
     * Updates localized property
     *
     * @param languages Languages of the datamodel, localized property has to be in language
     * @param data      Data to add
     * @param resource  Resource
     * @param property  Property
     */
    public static void updateLocalizedProperty(Set<String> languages,
                                               Map<String, String> data,
                                               Resource resource,
                                               Property property) {
        resource.removeAll(property);
        if (data != null && languages != null && !languages.isEmpty()) {
            addLocalizedProperty(languages, data, resource, property);
        }
    }

    /**
     * Update string property
     * If string is empty|blank value is removed
     *
     * @param resource Resource
     * @param property Property
     * @param value    Value
     */
    public static void updateStringProperty(Resource resource, Property property, String value) {
        resource.removeAll(property);
        if (value != null && !value.isBlank()) {
            resource.addProperty(property, value);
        }
    }

    public static void addLiteral(Resource resource, Property property, Object value) {
        if (value != null) {
            resource.addLiteral(property, value);
        }
    }

    public static void updateLiteral(Resource resource, Property property, Object value) {
        resource.removeAll(property);
        if (value != null) {
            resource.addLiteral(property, value);
        }
    }

    public static boolean hasType(Resource resource, Resource... type) {
        if (!resource.hasProperty(RDF.type)) {
            return false;
        }
        var typeList = resource.listProperties(RDF.type).toList();
        return Arrays.stream(type)
                .anyMatch(t -> typeList.stream().anyMatch(r -> r.getResource().equals(t)));
    }

    public static boolean isLibrary(Resource resource) {
        return hasType(resource, OWL.Ontology) && !hasType(resource, SuomiMeta.ApplicationProfile);
    }

    public static boolean isApplicationProfile(Resource resource) {
        return hasType(resource, SuomiMeta.ApplicationProfile);
    }

    public static boolean isTerminology(Resource resource) {
        return hasType(resource, SKOS.ConceptScheme);
    }

    public static Status getStatus(Resource resource) {
        var statusURI = propertyToString(resource, SuomiMeta.publicationStatus);
        if (statusURI != null) {
            return Status.valueOf(statusURI.substring(statusURI.lastIndexOf("/") + 1));
        }
        return null;
    }

    public static void addStatus(Resource resource, Status status) {
        if (status == null) {
            return;
        }
        resource.removeAll(SuomiMeta.publicationStatus);
        resource.addProperty(SuomiMeta.publicationStatus, ResourceFactory.createResource(getStatusUri(status)));
    }

    public static String getStatusUri(Status status) {
        return "http://uri.suomi.fi/codelist/interoperabilityplatform/interoperabilityplatform_status/code/" + status.name();
    }

    public static Status getStatusFromUri(String uri) {
        if (uri == null || uri.isBlank()) {
            throw new MappingError("Could not get status from uri");
        }
        return Status.valueOf(uri.substring(uri.lastIndexOf("/") + 1));
    }

    /**
     * Adds property to the resource as RDFList. An old list with the same property will be removed first.
     * @param resource Resource
     * @param property Property where list will be added
     * @param values list of values
     */
    public static <T extends RDFNode> void addListProperty(Resource resource, Property property, List<T> values) {
        removeList(resource, property);
        if (values.isEmpty()) {
            return;
        }

        var newList = resource.getModel().createList(values.iterator());
        resource.addProperty(property, newList);
    }

    /**
     * Returns RDFList. If resource doesn't have property or object is not RDFList, exception will be thrown
     */
    public static RDFList getList(Resource resource, Property property) {
        if (resource.hasProperty(property)) {
            var obj = resource.getProperty(property).getObject();
            if (!obj.canAs(RDFList.class)) {
                throw new MappingError(String.format("Property %s in resource %s is not RDFList",
                        property, resource));
            }
            return resource.getModel().getList(obj.asResource());
        }

        throw new MappingError(String.format("Creating RDFList failed. Property %s missing in resource %s",
                property, resource));
    }

    /**
     * Gets RDFList values mapped as resources. If resource doesn't have property, or it's not RDFList, an empty
     * list will be returned
     */
    public static List<Resource> getResourceList(Resource resource, Property property) {
        if (resource.hasProperty(property)) {
            var obj = resource.getProperty(property).getObject();
            if (!obj.canAs(RDFList.class)) {
               return new ArrayList<>();
            }
            return resource.getModel().getList(obj.asResource())
                    .asJavaList()
                    .stream()
                    .map(RDFNode::asResource)
                    .toList();
        }
        return new ArrayList<>();
    }

    /**
     * Removes list and its anonymous resources
     */
    public static void removeList(Resource resource, Property property) {
        if (resource.hasProperty(property)) {
            var obj = resource.getProperty(property).getObject();
            if (obj.canAs(RDFList.class)) {
                var list = resource.getModel().getList(obj.asResource());
                var iterator = list.iterator();

                while (iterator.hasNext()) {
                    var i = iterator.next();
                    if (i.isResource() && i.asResource().isAnon()) {
                        removeAllLists(i.asResource());
                        resource.getModel().removeAll(i.asResource(), null, null);
                    }
                }
                list.removeList();
                resource.removeAll(property);
            }
        }
    }

    /**
     * Removes all list properties from the resource
     */
    public static void removeAllLists(Resource resource) {
        resource.listProperties()
                .filterKeep(r -> r.getObject().canAs(RDFList.class))
                .toList()
                .forEach(prop -> removeList(resource, prop.getPredicate()));
    }
}
