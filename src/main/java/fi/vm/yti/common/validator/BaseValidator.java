package fi.vm.yti.common.validator;

import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.Annotation;
import java.util.Map;

public abstract class BaseValidator implements Annotation {

    private boolean constraintViolationAdded;

    @Override
    public Class<? extends Annotation> annotationType() {
        return BaseValidator.class;
    }

    /**
     * Add constraint violation to the constraint validator context
     *
     * @param context  Constraint validator context
     * @param message  Message
     * @param property Property
     */
    public void addConstraintViolation(ConstraintValidatorContext context, String message, String property) {
        if (!this.constraintViolationAdded) {
            context.disableDefaultConstraintViolation();
            this.constraintViolationAdded = true;
        }

        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(property)
                .addConstraintViolation();
    }

    public boolean isConstraintViolationAdded() {
        return constraintViolationAdded;
    }

    public void setConstraintViolationAdded(boolean constraintViolationAdded) {
        this.constraintViolationAdded = constraintViolationAdded;
    }

    public void checkRequiredLocalizedValue(ConstraintValidatorContext context, Map<String, String> value, String property) {
        if (value == null || value.isEmpty() || value.values().stream().anyMatch(v -> v == null || v.isBlank())) {
            addConstraintViolation(context, ValidationConstants.MSG_VALUE_MISSING, property);
        } else {
            value.forEach((lang, v) -> checkCommonTextField(context, v, property));
        }
    }

    public void checkCommonTextField(ConstraintValidatorContext context, String value, String property) {
        if (value != null && value.length() > ValidationConstants.TEXT_FIELD_MAX_LENGTH) {
            addConstraintViolation(context, ValidationConstants.MSG_OVER_CHARACTER_LIMIT
                                            + ValidationConstants.TEXT_FIELD_MAX_LENGTH, property);
        }
    }

    public void checkCommonTextArea(ConstraintValidatorContext context, String value, String property) {
        if (value != null && value.length() > ValidationConstants.TEXT_AREA_MAX_LENGTH) {
            addConstraintViolation(context, ValidationConstants.MSG_OVER_CHARACTER_LIMIT
                                            + ValidationConstants.TEXT_AREA_MAX_LENGTH, property);
        }
    }

    public void checkNotNull(ConstraintValidatorContext context, Object value, String property) {
        if (value == null) {
            addConstraintViolation(context, ValidationConstants.MSG_VALUE_MISSING, property);
        }
    }

}
