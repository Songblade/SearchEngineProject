import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;

public class HashTableTest {

    // I am pretty sure I need a constructor to run my tests properly
    public HashTableTest() {
        // This should do nothing
    }

    // I will now figure out what sort of tests I need
    // I need to check that if I add a key-variable pair, get returns the value
    @Test
    public void getReturnWorks() {
        HashTable<String, Integer> table = new HashTableImpl<>();
        table.put("Arranis", 10);
        assertEquals(10, table.get("Arranis"));
    }
    // I need to check that this works for several classes, I choose String and Integer in both orders
    @Test
    public void getReturnWorksInteger() {
        HashTable<Integer, Double> table = new HashTableImpl<>();
        table.put(3, 3.14156);
        assertEquals(3.14156, table.get(3));
    }
    // I need to make sure that this works even if I have several keys with the same hashCode (will need to
    // calculate first)
    @Test
    public void getReturnWorksWithDuplicates() {
        HashTable<Integer, String> table = new HashTableImpl<>();
        table.put(4, "The best number");
        table.put(-4, "A better number");
        assertEquals("The best number", table.get(4));
        assertEquals("A better number", table.get(-4));
    }
    // I need to make sure put with null successfully deletes the value (i.e. get returns null)
    @Test
    public void getReturnWorksNull() {
        HashTable<Integer, Double> table = new HashTableImpl<>();
        table.put(3, 3.14156);
        table.put(3, null);
        assertNull(table.get(3));
    }
    // I need to test that get null returns null
    @Test
    public void getNullWorksNull() {
        HashTable<Integer, Double> table = new HashTableImpl<>();
        assertNull(table.get(null));
    }
    // I need to make sure get returns null even when something was never added, even if something with the same hash was
    @Test
    public void getReturnWorksNullWithDuplicates() {
        HashTable<Integer, String> table = new HashTableImpl<>();
        table.put(4, "The best number");
        assertNull(table.get(-4));
    }
    // I need to make sure put returns properly if it wasn't there first
    @Test
    public void putReturnWorks() {
        HashTable<Integer, String> table = new HashTableImpl<>();
        table.put(4, "The best number");
        assertEquals("The best number", table.put(4, "Some numbers are more equal than others"));
    }
    // I need to make sure put returns null if this was the first of that key, even if there was another key first
    @Test
    public void putReturnWorksNull() {
        HashTable<Integer, String> table = new HashTableImpl<>();
        assertNull(table.put(-4, "An even better number"));
    }
    // I need to make sure that is true even if there is another key with the same hashcode
    @Test
    public void putReturnWorksNullWithDuplicates() {
        HashTable<Integer, String> table = new HashTableImpl<>();
        table.put(4, "The best number");
        assertNull(table.put(-4, "An even better number"));
    }

}