package com.wikia.calabash.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;


public class InEnumValidator implements ConstraintValidator<InEnum, Object> {
    private Set<String> allows;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(InEnum constraintAnnotation) {
        try {
            Class<Enum> value = (Class<Enum>) constraintAnnotation.value();
            Enum[] enumConstants = value.getEnumConstants();

            String fieldName = constraintAnnotation.field();
            allows = new HashSet<>();
            for (Enum enumConstant : enumConstants) {
                if (fieldName.isEmpty()) {
                    allows.add(enumConstant.name());
                } else {
                    Field field = value.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object o = field.get(enumConstant);
                    allows.add(o.toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (allows == null) {
            return false;
        }
        if (value == null || value.toString().isEmpty()) {
            return false;
        }
        return allows.contains(value.toString());
    }

}
