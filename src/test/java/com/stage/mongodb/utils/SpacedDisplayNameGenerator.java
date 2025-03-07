package com.stage.mongodb.utils;

import org.junit.jupiter.api.DisplayNameGenerator;

import java.lang.reflect.Method;

public class SpacedDisplayNameGenerator implements DisplayNameGenerator {

    @Override
    public String generateDisplayNameForClass(Class<?> testClass) {
        return generateSpacedName(testClass.getSimpleName());
    }

    @Override
    public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
        return generateSpacedName(nestedClass.getSimpleName());
    }

    @Override
    public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
        return generateSpacedName(testMethod.getName());
    }

    private String generateSpacedName(String name) {
        StringBuilder result = new StringBuilder();
        for (char c : name.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (!result.isEmpty()) {
                    result.append(' ');
                }
            }
            result.append(c);
        }
        return result.toString();
    }
}