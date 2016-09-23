package hu.akarnokd.java9.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * Shortcuts to VarHandles.
 */
public final class VH {

    /** Cache VarHandle for Object[]. */
    static final VarHandle OBJECT_ARRAY;

    /** Cache VarHandle for int[]. */
    static final VarHandle INT_ARRAY;

    static {
        try {
            OBJECT_ARRAY = MethodHandles.arrayElementVarHandle(Object[].class);
            INT_ARRAY = MethodHandles.arrayElementVarHandle(int[].class);
        } catch (Exception ex) {
            throw new InternalError(ex);
        }
    }

    public static VarHandle objects() {
        return OBJECT_ARRAY;
    }

    public static VarHandle ints() {
        return INT_ARRAY;
    }

    /**
     * Finds the specified field in the target class of the given
     * field type and returns a VarHandle to it
     * @param clazz
     * @param field
     * @param type
     * @return the VarHandle
     * @throws InternalError if the lookup fails for some reason
     */
    public static VarHandle field(Class<?> clazz, String field, Class<?> type) {
        try {
            return MethodHandles.lookup().in(clazz)
                    .findVarHandle(clazz, field, type);
        } catch (Exception ex) {
            throw new InternalError(ex);
        }
    }

    /**
     * Finds an {@code int} field in the target class and returns a VarHandle to it.
     * @param clazz
     * @param field
     * @return the VarHandle
     */
    public static VarHandle intField(Class<?> clazz, String field) {
        return field(clazz, field, Integer.TYPE);
    }

    /**
     * Finds an {@code long} field in the target class and returns a VarHandle to it.
     * @param clazz
     * @param field
     * @return the VarHandle
     */
    public static VarHandle longField(Class<?> clazz, String field) {
        return field(clazz, field, Long.TYPE);
    }
}
