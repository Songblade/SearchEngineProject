package edu.yu.cs.com1320.project.stage4;

import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MinHeapImplTest {

    private MinHeapImpl<TestClass> heap;
    private TestClass[] stuff;

    public MinHeapImplTest() {
        stuff = new TestClass[4];
        heap = new MinHeapImpl<>();
        for (int i = 0; i < 4; i++) {
            stuff[i] = new TestClass(i * 4);
            heap.insert(stuff[i]);
        }
    }

    // tests for reHeapify, though really, all tests are for reHeapify
    // Test that if I call reHeapify when the object can't change, nothing happens
    @Test
    public void reHeapifyUnchanging() {
        MinHeapImpl<Integer> intHeap = new MinHeapImpl<>();
        intHeap.insert(1);
        intHeap.insert(2);
        intHeap.insert(0);
        intHeap.reHeapify(0);
        assertEquals(0, intHeap.remove());
        intHeap.reHeapify(1);
        assertEquals(1, intHeap.remove());
    }

    // Test that if I call reHeapify when the object could have changed but didn't, nothing happens
    @Test
    public void reHeapifyUnchanged() {
        heap.reHeapify(stuff[1]);
        heap.reHeapify(stuff[0]);
        assertEquals(stuff[0], heap.remove());
        assertEquals(stuff[1], heap.remove());
    }

    // Test that if I call reHeapify when the object changed but would still be at the top, nothing happens
    @Test
    public void reHeapifySameRangeTop() {
        stuff[0].id = 2;
        heap.reHeapify(stuff[0]);
        assertEquals(stuff[0], heap.remove());
        assertEquals(stuff[1], heap.remove());
    }

    // Same as above, but object is further down
    @Test
    public void reHeapifySameRangeLater() {
        stuff[1].id = 2;
        heap.reHeapify(stuff[1]);
        assertEquals(stuff[0], heap.remove());
        assertEquals(stuff[1], heap.remove());
    }

    // Test that if I call reHeapify so that the object should no longer be on top, it is no longer on top
    @Test
    public void reHeapifyPushDownTop() {
        stuff[0].id = 6;
        heap.reHeapify(stuff[0]);
        assertEquals(stuff[1], heap.remove());
    }

    // Test that in the above scenario, the object is still in the array, and is in the new position
    // Test that if I call reHeapify so that the object should no longer be on top, it is no longer on top
    @Test
    public void reHeapifyPushDownTopStillThere() {
        stuff[0].id = 6;
        heap.reHeapify(stuff[0]);
        assertEquals(stuff[1], heap.remove());
        assertEquals(stuff[0], heap.remove());
    }

    // Test that can push down more than 1 at the top
    @Test
    public void reHeapifyPushDownTopWayDown() {
        stuff[0].id = 14;
        heap.reHeapify(stuff[0]);
        assertEquals(stuff[1], heap.remove());
        assertEquals(stuff[2], heap.remove());
        assertEquals(stuff[3], heap.remove());
        assertEquals(stuff[0], heap.remove());
    }

    // Test that if I call reHeapify to push down an object that isn't at the top, it appears in the new location
    @Test
    public void reHeapifyPushDownMiddle() {
        stuff[1].id = 10;
        heap.reHeapify(stuff[1]);
        assertEquals(stuff[0], heap.remove());
        assertEquals(stuff[2], heap.remove());
        assertEquals(stuff[1], heap.remove());
    }

    // Test that can push down more than 1 at the middle
    @Test
    public void reHeapifyPushDownMiddleWayDown() {
        stuff[1].id = 14;
        heap.reHeapify(stuff[1]);
        assertEquals(stuff[0], heap.remove());
        assertEquals(stuff[2], heap.remove());
        assertEquals(stuff[3], heap.remove());
        assertEquals(stuff[1], heap.remove());
    }

    // Test that if I call reHeapify to push up an object to the top, it is now at the top
    // Test that if I push something to the top, the previous champ is still in the next position
    @Test
    public void reHeapifyPushUpToTop() {
        stuff[1].id = -1;
        heap.reHeapify(stuff[1]);
        assertEquals(stuff[1], heap.remove());
        assertEquals(stuff[0], heap.remove());
    }

    // Test that can push up by more than 1 to top
    @Test
    public void reHeapifyPushWayUpToTop() {
        stuff[3].id = -1;
        heap.reHeapify(stuff[3]);
        assertEquals(stuff[3], heap.remove());
        assertEquals(stuff[0], heap.remove());
    }

    // Test that if I call reHeapify to push up an object up but not to the top, it is in its new position
    @Test
    public void reHeapifyPushUpToMiddle() {
        stuff[2].id = 2;
        heap.reHeapify(stuff[2]);
        assertEquals(stuff[0], heap.remove());
        assertEquals(stuff[2], heap.remove());
        assertEquals(stuff[1], heap.remove());
    }

    // Test that can push up by more than 1 to middle
    @Test
    public void reHeapifyPushWayUpToMiddle() {
        stuff[3].id = 2;
        heap.reHeapify(stuff[3]);
        assertEquals(stuff[0], heap.remove());
        assertEquals(stuff[3], heap.remove());
        assertEquals(stuff[1], heap.remove());
        assertEquals(stuff[2], heap.remove());
    }

    // Test that if I reheapify one to a level that doesn't make a difference, and another where it only makes
        // a difference now, the second is now on top
    @Test
    public void reHeapifySameRangeMattersLater() {
        stuff[0].id = 2;
        heap.reHeapify(stuff[0]);
        stuff[3].id = 1;
        heap.reHeapify(stuff[3]);
        assertEquals(stuff[3], heap.remove());
        assertEquals(stuff[0], heap.remove());
        assertEquals(stuff[1], heap.remove());
        assertEquals(stuff[2], heap.remove());
    }

    // Test that the above works in either order
    @Test
    public void reHeapifySameRangeMattersLaterAnyOrder() {
        stuff[3].id = 1;
        heap.reHeapify(stuff[3]);
        stuff[0].id = 2;
        heap.reHeapify(stuff[0]);
        assertEquals(stuff[3], heap.remove());
        assertEquals(stuff[0], heap.remove());
        assertEquals(stuff[1], heap.remove());
        assertEquals(stuff[2], heap.remove());
    }

    // Test that it doesn't work if I use an object that isn't comparable
    // this test is complete because when I tried to write it, the IDE was giving me problems that Object
        // is not comparable

    private static class TestClass implements Comparable<TestClass>{
        private int id;

        private TestClass(int id) {
            this.id = id;
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         *
         * <p>The implementor must ensure
         * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
         * for all {@code x} and {@code y}.  (This
         * implies that {@code x.compareTo(y)} must throw an exception iff
         * {@code y.compareTo(x)} throws an exception.)
         *
         * <p>The implementor must also ensure that the relation is transitive:
         * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
         * {@code x.compareTo(z) > 0}.
         *
         * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
         * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
         * all {@code z}.
         *
         * <p>It is strongly recommended, but <i>not</i> strictly required that
         * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
         * class that implements the {@code Comparable} interface and violates
         * this condition should clearly indicate this fact.  The recommended
         * language is "Note: this class has a natural ordering that is
         * inconsistent with equals."
         *
         * <p>In the foregoing description, the notation
         * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
         * <i>signum</i> function, which is defined to return one of {@code -1},
         * {@code 0}, or {@code 1} according to whether the value of
         * <i>expression</i> is negative, zero, or positive, respectively.
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         * is less than, equal to, or greater than the specified object.
         * @throws NullPointerException if the specified object is null
         * @throws ClassCastException   if the specified object's type prevents it
         *                              from being compared to this object.
         */
        @Override
        public int compareTo(TestClass o) {
            if (o == null) {
                throw new NullPointerException("Test object is null? Seriously? How is this even possible?");
            }
            return this.id - o.id;
        }

        @Override
        public String toString() {
            return "TestClass{" +
                    "id=" + id +
                    '}';
        }
    }

}
