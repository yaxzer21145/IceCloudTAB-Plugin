package com.icecloud.tab.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ReflectionUtil {

    private static final ConcurrentMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    private ReflectionUtil() {
    }

    public static Class<?> getClass(String className) {
        return CLASS_CACHE.computeIfAbsent(className, key -> {
            try {
                return Class.forName(key);
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
    }

    public static Class<?> getNMSClass(String className) {
        String nmsVersion = VersionUtils.getNMSVersion();
        return getClass("net.minecraft.server." + nmsVersion + "." + className);
    }

    public static Class<?> getCraftBukkitClass(String className) {
        String nmsVersion = VersionUtils.getNMSVersion();
        return getClass("org.bukkit.craftbukkit." + nmsVersion + "." + className);
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        if (clazz == null) return null;
        String key = clazz.getName() + "#" + methodName;
        return METHOD_CACHE.computeIfAbsent(key, k -> {
            try {
                Method method = clazz.getDeclaredMethod(methodName, paramTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                try {
                    Method method = clazz.getMethod(methodName, paramTypes);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException e2) {
                    return null;
                }
            }
        });
    }

    public static Object invokeMethod(Object obj, String methodName, Object... args) {
        if (obj == null) return null;
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }
        Method method = getMethod(obj.getClass(), methodName, paramTypes);
        if (method == null) return null;
        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object invokeStaticMethod(Class<?> clazz, String methodName, Object... args) {
        if (clazz == null) return null;
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }
        Method method = getMethod(clazz, methodName, paramTypes);
        if (method == null) return null;
        try {
            return method.invoke(null, args);
        } catch (Exception e) {
            return null;
        }
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        if (clazz == null) return null;
        String key = clazz.getName() + "#" + fieldName;
        return FIELD_CACHE.computeIfAbsent(key, k -> {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                try {
                    Field field = clazz.getField(fieldName);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException e2) {
                    return null;
                }
            }
        });
    }

    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null) return null;
        Field field = getField(obj.getClass(), fieldName);
        if (field == null) return null;
        try {
            return field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setFieldValue(Object obj, String fieldName, Object value) {
        if (obj == null) return;
        Field field = getField(obj.getClass(), fieldName);
        if (field == null) return;
        try {
            field.set(obj, value);
        } catch (Exception ignored) {
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... paramTypes) {
        if (clazz == null) return null;
        String key = clazz.getName() + "#init(" + paramTypes.length + ")";
        return CONSTRUCTOR_CACHE.computeIfAbsent(key, k -> {
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor(paramTypes);
                constructor.setAccessible(true);
                return constructor;
            } catch (NoSuchMethodException e) {
                try {
                    Constructor<?> constructor = clazz.getConstructor(paramTypes);
                    constructor.setAccessible(true);
                    return constructor;
                } catch (NoSuchMethodException e2) {
                    return null;
                }
            }
        });
    }

    public static Object newInstance(Class<?> clazz, Object... args) {
        if (clazz == null) return null;
        Class<?>[] paramTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            paramTypes[i] = args[i].getClass();
        }
        Constructor<?> constructor = getConstructor(clazz, paramTypes);
        if (constructor == null) return null;
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            return null;
        }
    }

}