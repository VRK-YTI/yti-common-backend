package fi.vm.yti.common.validator;

public class ValidationConstants {

    protected ValidationConstants() {
        // constants
    }

    public static final String MSG_VALUE_MISSING = "should-have-value";
    public static final String MSQ_NOT_ALLOWED = "should-not-have-value";
    public static final String MSG_VALUE_INVALID = "invalid-value";
    public static final String MSG_NOT_ALLOWED_UPDATE =  "not-allowed-update";
    public static final String MSG_OVER_CHARACTER_LIMIT = "value-over-character-limit.";

    public static final int TEXT_FIELD_MAX_LENGTH = 150;
    public static final int EMAIL_FIELD_MAX_LENGTH = 320;
    public static final int TEXT_AREA_MAX_LENGTH = 5000;
    public static final int DOCUMENTATION_MAX_LENGTH = 50000;

    public static final int PREFIX_MIN_LENGTH = 2;
    public static final int PREFIX_MAX_LENGTH = 32;

    public static final String PREFIX_REGEX = "^[a-z][a-z0-9-_]{1,31}";

    public static final String RESOURCE_IDENTIFIER_REGEX = "^[a-zA-Z][a-zA-Z0-9-_]{1,119}";

}
