package com.iinitial.dmzautotrainer.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflect {
    public static Object get(Object target, String fieldName) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    public static void set(Object target, String fieldName, Object value) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    public static Object invoke(Object target, String methodName) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Method m = clazz.getDeclaredMethod(methodName);
                m.setAccessible(true);
                return m.invoke(target);
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Method not found: " + methodName);
    }
}