package edu.yu.cs.com1320.project;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import edu.yu.cs.com1320.project.impl.StackImpl;
public class StackTest {

    @Test
    public void testLargeStackOnAndOff() { // I will put on and then take off a bunch of things to the stack
        Stack<Integer> stack = new StackImpl<>();
        for (int i = 0; i < 1000000; i++) {
            stack.push(i);
        }
        for (int i = 999999; i >= 0; i--) {
            assertEquals(i, stack.pop());
        }
    }

    @Test
    public void popWorksEvenWhenAddedInTheMiddle() { // adding things, taking things out, then adding more things
        Stack<Integer> stack = new StackImpl<>();
        stack.push(1);
        stack.push(2);
        assertEquals(2, stack.pop());
        stack.push(3);
        assertEquals(3, stack.pop());
        assertEquals(1, stack.pop());
    }

    @Test
    public void popNullWhenEmpty() { // I will test both when it starts empty and when stuff are added and removed
        Stack<Integer> stack = new StackImpl<>();
        assertNull(stack.pop());
        stack.push(1);
        stack.pop();
        assertNull(stack.pop());
    }

    @Test
    public void canGoToZeroAndBack() { // Tests that we can remove all elements but still add more
        Stack<Integer> stack = new StackImpl<>();
        stack.push(1);
        stack.pop();
        assertNull(stack.pop());
        stack.push(2);
        assertEquals(2, stack.peek());
        assertEquals(2, stack.pop());
        assertNull(stack.pop());
    }

    @Test
    public void canDoDuplicates() {// Tests that can handle duplicate elements
        Stack<Integer> stack = new StackImpl<>();
        stack.push(1);
        stack.push(1);
        assertEquals(1, stack.pop());
        assertEquals(1, stack.pop());
        assertNull(stack.pop());
    }

    @Test
    public void peekDoesNotRemove() {
        Stack<Integer> stack = new StackImpl<>();
        stack.push(1);
        stack.push(2);
        stack.peek();
        assertEquals(2, stack.peek());
        assertEquals(2, stack.pop());
    }

    @Test
    public void peekWorksEvenWhenAddedInTheMiddle() { // adding things, taking things out, then adding more things
        Stack<Integer> stack = new StackImpl<>();
        stack.push(1);
        stack.push(2);
        assertEquals(2, stack.pop());
        stack.push(3);
        assertEquals(3, stack.peek());
        assertEquals(3, stack.pop());
        assertEquals(1, stack.peek());
        assertEquals(1, stack.pop());
    }

    @Test
    public void peekNullWhenEmpty() { // both at the beginning and later on
        Stack<Integer> stack = new StackImpl<>();
        assertNull(stack.peek());
        stack.push(1);
        stack.pop();
        assertNull(stack.peek());
    }

    @Test
    public void testLargeStackSize() { // I will put on and then take off a bunch of things to the stack
        // testing size when growing, and again when shrinking
        Stack<Integer> stack = new StackImpl<>();
        for (int i = 0; i < 1000000; i++) {
            assertEquals(i, stack.size());
            stack.push(i);
        }
        assertEquals(1000000, stack.size());
        for (int i = 999999; i >= 0; i--) {
            stack.pop();
            assertEquals(i, stack.size());
        }
    }

    @Test
    public void shortTestingEverythingWithDifDataType() {
        Stack<String> stack = new StackImpl<>();
        assertNull(stack.pop());
        for (int i = 0; i < 10; i++) {
            assertEquals(i, stack.size());
            stack.push(Integer.toString(i));
        }
        assertEquals(10, stack.size());
        for (int i = 9; i >= 0; i--) {
            assertEquals(Integer.toString(i), stack.peek());
            assertEquals(Integer.toString(i), stack.pop());
            assertEquals(i, stack.size());
        }
    }

}
