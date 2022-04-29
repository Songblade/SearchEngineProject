package edu.yu.cs.com1320.project;

import edu.yu.cs.com1320.project.impl.MinHeapImpl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MinHeapProtectedTest {
    // Since protected methods that I have no part in making are being tested, I need to make sure they actually work
    private MinHeap<Integer> heap;
    private Integer[] stuff;

    public MinHeapProtectedTest() {
        stuff = new Integer[4];
        heap = new MinHeapImpl<>();
        for (int i = 0; i < 4; i++) {
            stuff[i] = i * 4;
            heap.insert(stuff[i]);
        }
    }

    // I will start by testing isEmpty(), since it should be the easiest to test
    // check that isEmpty returns true when I first create a MinHeap
    @Test
    public void isEmptyStartsTrue() {
        MinHeap<Integer> testHeap = new MinHeapImpl<>();
        assertTrue(testHeap.isEmpty());
    }

    // make sure that isEmpty returns false when I add stuff
    @Test
    public void isEmptyFalseWhenAddStuff() {
        MinHeap<Integer> testHeap = new MinHeapImpl<>();
        testHeap.insert(1);
        assertFalse(testHeap.isEmpty());
        assertFalse(heap.isEmpty());
    }

    // make sure that isEmpty returns false when I remove stuff, if there still is stuff
    @Test
    public void isEmptyFalseWhenRemoveStuff() {
        heap.remove();
        heap.remove();
        heap.remove();
        assertFalse(heap.isEmpty());
    }

    // make sure that if I get rid of everything in the MinHeap, isEmpty returns true
    @Test
    public void isEmptyTrueWhenRemoveAll() {
        heap.remove();
        heap.remove();
        heap.remove();
        assertFalse(heap.isEmpty());
        heap.remove();
        assertTrue(heap.isEmpty());
    }

    // tests for isGreater
    // make sure isGreater true when i > j
    @Test
    public void isGreaterTrueWhenGreater() {
        assertTrue(heap.isGreater(2, 1));
    }

    // make sure isGreater false when i == j
    @Test
    public void isGreaterFalseWhenEqual() {
        assertFalse(heap.isGreater(1, 1));
    }

    // make sure isGreater false when i < j
    @Test
    public void isGreaterFalseWhenLess() {
        assertFalse(heap.isGreater(1, 2));
    }

    // make sure isGreater works for non-Integers
    @Test
    public void isGreaterWorksNonInts() {
        MinHeap<Character> testHeap = new MinHeapImpl<>();
        testHeap.insert('a');
        testHeap.insert('z');
        assertTrue(heap.isGreater(2, 1));
        assertFalse(heap.isGreater(1, 1));
        assertFalse(heap.isGreater(1, 2));
    }

    // now testing swap
    // make sure that swap swaps stuff, it is really that simple
    @Test
    public void swapWorks() {
        heap.swap(1, 3);
        assertEquals(8, heap.remove());
    }

    // I think I actually can test upheap and downheap after all, using the swap method to mess things up
        // and then the upheap and downheap methods to fix everything

    // testing upheap
    // make sure can upheap something swapped back to where it was before
    @Test
    public void upHeapWorks() {
        heap.swap(1, 3);
        heap.upHeap(3);
        assertEquals(0, heap.remove());
    }

    // make sure can't upheap something in the right position
    @Test
    public void upHeapDoesntMoveWhenGood() {
        heap.upHeap(3);
        assertEquals(0, heap.remove());
        assertEquals(4, heap.remove());
        assertEquals(8, heap.remove());
    }

    // testing downHeap
    // make sure can downHeap something swapped back to where it was before, when swapped down 1
    @Test
    public void downHeapWorks1() {
        heap.swap(1, 2);
        heap.downHeap(1);
        assertEquals(0, heap.remove());
        assertEquals(4, heap.remove());
    }

    // make sure can't downheap something in the right position
    @Test
    public void downHeapDoesntMoveWhenGood() {
        heap.downHeap(1);
        assertEquals(0, heap.remove());
        assertEquals(4, heap.remove());
        assertEquals(8, heap.remove());
    }

    // I could probably write more tests, but this is stupid, so I will just assume that I will find bugs
        // organically when testing documentstore


}
