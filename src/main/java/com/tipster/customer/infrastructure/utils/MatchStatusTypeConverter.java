package com.tipster.customer.infrastructure.utils;

import com.tipster.customer.domain.enums.MatchStatusType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts between Java MatchStatusType enum and database enum (both lowercase)
 * @deprecated This converter is no longer needed for MatchData as it uses @JdbcTypeCode(SqlTypes.NAMED_ENUM)
 * Still used by Match entity for backward compatibility
 */
@Deprecated
@Converter(autoApply = false)
public class MatchStatusTypeConverter implements AttributeConverter<MatchStatusType, String> {

    @Override
    public String convertToDatabaseColumn(MatchStatusType attribute) {
        if (attribute == null) {
            return null;
        }
        // Enum is now lowercase, return name directly
        return attribute.name();
    }

    @Override
    public MatchStatusType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        // Enum is now lowercase, parse directly
        try {
            return MatchStatusType.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // Handle unknown values gracefully
            return MatchStatusType.scheduled;
        }
    }
}
