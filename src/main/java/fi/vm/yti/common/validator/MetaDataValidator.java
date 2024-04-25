package fi.vm.yti.common.validator;

import fi.vm.yti.common.Constants;
import fi.vm.yti.common.dto.MetaDataDTO;
import fi.vm.yti.common.repository.BaseRepository;
import fi.vm.yti.common.service.FrontendService;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

public abstract class MetaDataValidator<R extends BaseRepository> extends BaseValidator {

    @Autowired
    FrontendService frontendService;

    private final R repository;

    protected MetaDataValidator(R repository) {
        this.repository = repository;
    }

    public abstract String getNamespacePrefix();

    /**
     * Check if prefix is valid, if updating prefix cannot be set
     *
     * @param context Constraint validator context
     * @param value DataModel
     */
    public void checkModelPrefix(ConstraintValidatorContext context, MetaDataDTO value, boolean update) {
        final var prefixPropertyLabel = "prefix";
        var prefix = value.getPrefix();

        checkPrefix(context, prefix, prefixPropertyLabel, update);

        if (repository.graphExists(getNamespacePrefix() + prefix + Constants.RESOURCE_SEPARATOR)) {
            // Checking if in use is different for data models and its resources so it is not in the above function
            addConstraintViolation(context, "prefix-in-use", prefixPropertyLabel);
        }
    }

    public void checkPrefix(ConstraintValidatorContext context, final String value, String propertyName, boolean update) {
        if (update && value != null) {
            addConstraintViolation(context, ValidationConstants.MSG_NOT_ALLOWED_UPDATE, propertyName);
        } else if (!update && (value == null || value.isBlank())) {
            addConstraintViolation(context, ValidationConstants.MSG_VALUE_MISSING, propertyName);
        } else if (value != null && !value.matches(ValidationConstants.PREFIX_REGEX)) {
            addConstraintViolation(context, ValidationConstants.MSG_VALUE_INVALID, propertyName);
        } else if (value != null && (
                value.length() < ValidationConstants.PREFIX_MIN_LENGTH
                || value.length() > ValidationConstants.PREFIX_MAX_LENGTH)) {
            addConstraintViolation(context, propertyName + "-character-count-mismatch", propertyName);
        }
    }

    /**
     * Check if labels are valid
     *
     * @param context  Constraint validator context
     * @param metadata Data Model
     */
    public void checkLabels(ConstraintValidatorContext context, MetaDataDTO metadata) {
        final var labelPropertyLabel = "label";
        var labels = metadata.getLabel();
        var languages = metadata.getLanguages();

        checkRequiredLocalizedValue(context, labels, "label");

        if (labels.size() != languages.size()) {
            addConstraintViolation(context, "label-language-count-mismatch", labelPropertyLabel);
        } else {
            labels.forEach((key, value) -> {
                if (!languages.contains(key)) {
                    addConstraintViolation(context, "language-not-in-language-list." + key, labelPropertyLabel);
                }
                checkCommonTextField(context, value, labelPropertyLabel);
            });
        }
    }

    /**
     * Check if descriptions are valid
     *
     * @param context  Constraint validator context
     * @param metadata Data model
     */
    public void checkDescription(ConstraintValidatorContext context, MetaDataDTO metadata) {
        var description = metadata.getDescription();
        var languages = metadata.getLanguages();
        description.forEach((key, value) -> {
            if (!languages.contains(key)) {
                addConstraintViolation(context, "language-not-in-language-list." + key, "description");
            }
            checkCommonTextArea(context, value, "description");
        });
    }

    /**
     * Check if organizations are valid
     *
     * @param context Constraint validator context
     * @param value   DataModel
     */
    public void checkOrganizations(ConstraintValidatorContext context, MetaDataDTO value) {
        var organizations = value.getOrganizations();
        if (value.getOrganizations().isEmpty()) {
            addConstraintViolation(context, ValidationConstants.MSG_VALUE_MISSING, "organization");
            return;
        }
        var existingOrganizations = frontendService.getOrganizations("en", true);
        organizations.forEach(org -> {
            var organization = existingOrganizations.stream().filter(o -> o.getId().equals(org.toString())).findFirst();

            if (organization.isEmpty()) {
                addConstraintViolation(context, "does-not-exist." + org, "organizations");
            }
        });
    }

    /**
     * Check if languages are valid
     *
     * @param context Constraint validator context
     * @param value   Graph metadata
     */
    public void checkLanguages(ConstraintValidatorContext context, MetaDataDTO value) {
        var languages = value.getLanguages();

        if (languages.isEmpty()) {
            addConstraintViolation(context, ValidationConstants.MSG_VALUE_MISSING, "languages");
            return;
        }

        checkLanguageTags(context, languages, "languages");
    }

    public void checkLanguageTags(ConstraintValidatorContext context, Collection<String> languages, String property) {
        languages.forEach(language -> {
            //Matches RFC-4646
            if (!language.matches("^[a-z]{2,3}(?:-[A-Z]{2,3}(?:-[a-zA-Z]{4})?)?$")) {
                addConstraintViolation(context, "does-not-match-rfc-4646", property);
            }
        });
    }

    /**
     * Check if groups are valid
     *
     * @param context Constraint validator context
     * @param value   DataModel
     */
    public void checkGroups(ConstraintValidatorContext context, MetaDataDTO value) {
        var groups = value.getGroups();
        if (groups.isEmpty()) {
            addConstraintViolation(context, ValidationConstants.MSG_VALUE_MISSING, "groups");
            return;
        }
        var existingGroups = frontendService.getServiceCategories("en");
        groups.forEach(group -> {
            var grp = existingGroups.stream().filter(g -> g.getIdentifier().equals(group)).findFirst();
            if (grp.isEmpty()) {
                addConstraintViolation(context, "does-not-exist." + group, "groups");
            }
        });
    }

    /**
     * Check contact,
     *
     * @param context Constraint validator context
     * @param value   DataModel
     */
    public void checkContact(ConstraintValidatorContext context, MetaDataDTO value) {
        var contact = value.getContact();
        if (contact != null && contact.length() > ValidationConstants.EMAIL_FIELD_MAX_LENGTH) {
            addConstraintViolation(context, ValidationConstants.MSG_OVER_CHARACTER_LIMIT, "contact");
        }
    }
}
