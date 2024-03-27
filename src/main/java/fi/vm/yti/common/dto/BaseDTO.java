package fi.vm.yti.common.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Base DTO class for storing resources
 */
public abstract class BaseDTO {
    private String identifier;
    private String editorialNote;

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
