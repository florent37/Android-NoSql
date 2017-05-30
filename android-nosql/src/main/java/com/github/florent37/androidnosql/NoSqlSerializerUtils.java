package com.github.florent37.androidnosql;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by florentchampigny on 29/05/2017.
 */

public class NoSqlSerializerUtils {
    public static boolean isAcceptableObject(Object object) {
        return isPrimitiveObject(object) ||
                object instanceof NoSql.Node;
    }

    public static Class genericType(Field field){
        final ParameterizedType listType = (ParameterizedType) field.getGenericType();
        final Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];
        return listClass;
    }

    public static boolean isPrimitiveObject(Object object) {
        return object instanceof String ||
                object instanceof Integer ||
                object instanceof Double ||
                object instanceof Float ||
                object instanceof Long;
    }

    public static boolean isPrimitiveField(Field field) {
        return isPrimitive(field.getType());
    }

    public static boolean isPrimitive(Class type) {
        return
                Integer.class.isAssignableFrom(type) ||
                        Long.class.isAssignableFrom(type) ||
                        Double.class.isAssignableFrom(type) ||
                        Float.class.isAssignableFrom(type) ||
                        Boolean.class.isAssignableFrom(type) ||
                        String.class.isAssignableFrom(type);
    }

    public static boolean isCollection(Field field) {
        return List.class.isAssignableFrom(field.getType());
    }

    public static boolean isArray(Field field) {
        return field.getType().isArray();
    }

    public static boolean isInterface(Field field) {
        return field.getType().isInterface();
    }


    public static Set<Field> getAllFields(Class theClass) {
        final Set<Field> allFields = new HashSet<>();
        Class tmpClass = theClass;
        while (tmpClass != Object.class) {
            allFields.addAll(Arrays.asList(tmpClass.getDeclaredFields()));
            tmpClass = tmpClass.getSuperclass();
        }
        return allFields;
    }

}
