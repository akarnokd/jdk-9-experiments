package hu.akarnokd.java9.queue;

import static org.junit.Assert.*;
import org.junit.Test;

public class SpscArrayQueueTest {

    public static void main(String[] args) {
        SpscArrayQueue<Integer> queue = new SpscArrayQueue<Integer>(4);

        assertTrue(queue.isEmpty());

        assertTrue(queue.offer(1));
        assertTrue(queue.offer(2));
        assertTrue(queue.offer(3));
        assertTrue(queue.offer(4));
        assertFalse(queue.offer(5));

        assertFalse(queue.isEmpty());

        assertEquals(1, queue.poll().intValue());
        assertEquals(2, queue.poll().intValue());
        assertEquals(3, queue.poll().intValue());
        assertEquals(4, queue.poll().intValue());
        assertNull(queue.poll());

        assertTrue(queue.isEmpty());
    }

    @Test
    public void test() {
    }

}
