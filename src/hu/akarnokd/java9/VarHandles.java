package hu.akarnokd.java9;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicBoolean;

public class VarHandles {

    static int field;
    static int[] array = new int[20];

    static final VarHandle FIELD;

    static final VarHandle ARRAY;

    static {
        try {
            FIELD = MethodHandles.lookup()
            .in(VarHandles.class).findStaticVarHandle(VarHandles.class, "field", Integer.TYPE);

            ARRAY = MethodHandles.arrayElementVarHandle(int[].class);
        } catch (Exception ex) {
            throw new InternalError(ex);
        }
    }

    public static void main(String[] args) throws Exception {

        int i = (int)FIELD.getVolatile(/* static don't need an instance param! */);

        System.out.println(i);

        FIELD.getAndAdd(/* static don't need an instance param! */ 5);

        System.out.println(field);

        System.out.println(ARRAY.getAndAdd(array, 5, 5));

        System.out.println(ARRAY.getAndAdd(array, 5, 5));


        AtomicBoolean b = new AtomicBoolean();

        System.out.println(b.getPlain());

        b.setPlain(true);

        System.out.println(b.getOpaque());

        b.lazySet(false);

        b.compareAndExchange(false, true);
    }
}
