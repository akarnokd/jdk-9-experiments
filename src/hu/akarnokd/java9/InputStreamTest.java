package hu.akarnokd.java9;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class InputStreamTest {

    public static void main(String[] args) throws Exception {
        byte[] b = new byte[1024];
        ThreadLocalRandom.current().nextBytes(b);

        ByteArrayInputStream bin = new ByteArrayInputStream(b);

        byte[] u = bin.readAllBytes();

        System.out.println(Arrays.equals(b, u));
    }
}
