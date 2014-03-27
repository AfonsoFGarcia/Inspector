package ist.meic.pa.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * PrimitiveWrapper obtains the object type equivalent to the primitive type in java.<br>
 * <br>
 * It uses source code removed from the Apache Commons Lang Library, in the class org.apache.commons.lang3.ClassUtils.
 */
public class PrimitiveWrapper {
    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<Class<?>, Class<?>>();
    static {
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }

    public static Class<?> getWrapper(Class<?> type) {
        Class<?> convertedClass = type;
        if (type != null && type.isPrimitive()) {
            convertedClass = primitiveWrapperMap.get(type);
        }
        return convertedClass;
    }
}
