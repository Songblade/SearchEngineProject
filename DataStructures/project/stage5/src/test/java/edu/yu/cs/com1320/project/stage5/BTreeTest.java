package edu.yu.cs.com1320.project.stage5;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BTreeTest {
    // Now I will need to figure out all the things that I need to test
    // I am having a hard time thinking of tests, which is weird, let's do these and hope I didn't miss anything

    // tests for put and get
    // I need to make sure I can put and get a bunch of times without anything breaking
    @Test
    public void canPutAndGetABunchOfTimes() {
        BTree<Integer, String> tree = new BTreeImpl<>();
        for (int i = 0; i < 1000000; i++) {
            tree.put(i, "" + i);
        }
        for (int i = 0; i < 1000000; i++) {
            assertEquals("" + i, tree.get(i));
        } // testing forwards and backwards, make sure order doesn't matter
        for (int i = 999999; i >= 0; i--) {
            assertEquals("" + i, tree.get(i));
        }
    }

    // test get returns null if this value isn't in the tree
    @Test
    public void testGetReturnsNullIfNotThere() {
        BTree<Integer, String> tree = new BTreeImpl<>();
        tree.put(3, "Cheese");
        assertNull(tree.get(4));
        tree.put(3, null);
        assertNull(tree.get(3));
    }

    // test put returns null if this is the first one
    @Test
    public void testPutReturnsNull() {
        BTree<Integer, String> tree = new BTreeImpl<>();
        assertNull(tree.put(3, "Cheese"));
        assertNull(tree.put(4, "Smoked Cheese"));
    }

    // test can put to replace
    @Test
    public void testPutCanReplace() {
        BTree<Integer, String> tree = new BTreeImpl<>();
        tree.put(3, "Cheese");
        tree.put(4, "Smoked cheese");
        assertEquals("Cheese", tree.get(3));
        tree.put(3, "Cheddar");
        assertEquals("Cheddar", tree.get(3));
    }

    // test put to replace gives back what was deleted
    @Test
    public void testPutReplaceReturnsPrevious() {
        BTree<Integer, String> tree = new BTreeImpl<>();
        tree.put(3, "Cheese");
        tree.put(4, "Smoked cheese");
        assertEquals("Cheese", tree.put(3, "Cheddar"));
    }

    // test can put null to delete
    @Test
    public void testPutNullDeletes() {
        BTree<Integer, String> tree = new BTreeImpl<>();
        tree.put(3, "Cheese");
        tree.put(4, "Smoked cheese");
        assertEquals("Cheese", tree.get(3));
        tree.put(3, null);
        assertNull(tree.get(3));
    }

    // test put null to delete gives back what was deleted
    @Test
    public void testPutNullReturns() {
        BTree<Integer, String> tree = new BTreeImpl<>();
        tree.put(3, "Cheese");
        tree.put(4, "Smoked cheese");
        assertEquals("Cheese", tree.get(3));
        assertEquals("Cheese", tree.put(3, null));
    }

    // tests for moveToDisk
    // test that move to disk actually gets there
    // I need to make sure I can move to disk and get still works
    // When I get from the disk, it is no longer in the disk (using my own persistence manager)

}
