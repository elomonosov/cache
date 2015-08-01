package ru.elomonosov.util;

public class ClassNameUtil {

    private ClassNameUtil() {
    }

    public static String getCurrentClassName() {
        try {
            throw new RuntimeException();
        } catch (RuntimeException e) {
            return e.getStackTrace()[1].getClassName();
        }
    }
}
