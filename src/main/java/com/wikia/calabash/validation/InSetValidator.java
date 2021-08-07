package com.wikia.calabash.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class InSetValidator implements ConstraintValidator<InSet, Object> {
    private Set<String> allows;

    @Override
    public void initialize(InSet constraintAnnotation) {
        try {
            String[] value = constraintAnnotation.value();
            allows = new HashSet<>(Arrays.asList(value));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (allows == null || value == null) {
            return false;
        }
        return allows.contains(value.toString());
    }

}
