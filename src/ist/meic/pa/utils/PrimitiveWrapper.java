package ist.meic.pa.utils;

import java.util.HashMap;

/**
 * PrimitiveWrapper obtains the object type equivalent to the primitive type in java.<br>
 * <br>
 * It uses one of two ways:<br>
 * <ul>
 * <li>The Apache Commons Lang library</li>
 * <li>The solution proposed at <a href="http://stackoverflow.com/a/3474206">http://stackoverflow.com/a/3474206</a></li>
 * </ul>
 * <br>
 * <br>
 * This solution is pending approval by the professors.
 */
public class PrimitiveWrapper {
    private final static HashMap<Class<?>, Class<?>> typeConverter = new HashMap<Class<?>, Class<?>>();
    private static boolean useLibrary = false;

    static {
        typeConverter.put(boolean.class, Boolean.class);
        typeConverter.put(short.class, Short.class);
        typeConverter.put(int.class, Integer.class);
        typeConverter.put(long.class, Long.class);
        typeConverter.put(float.class, Float.class);
        typeConverter.put(double.class, Double.class);
        typeConverter.put(byte.class, Byte.class);
    }

    public static void useApacheLibrary(boolean u) {
        useLibrary = u;
    }

    public static Class<?> getWrapper(Class<?> type) {
        if (useLibrary) {
            return org.apache.commons.lang3.ClassUtils.primitiveToWrapper(type);
        } else {
            return typeConverter.get(type);
        }
    }

}
