package fi.vm.yti.common.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 * Base DTO class for storing resources
 */
public abstract class BaseDTO {
    private Map<String, String> label;
    private String identifier;
    private String editorialNote;

    public Map<String, String> getLabel() {
        return label;
    }

    public void setLabel(Map<String, String> label) {
        this.label = label;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEditorialNote() {
        return editorialNote;
    }

    public void setEditorialNote(String editorialNote) {
        this.editorialNote = editorialNote;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
