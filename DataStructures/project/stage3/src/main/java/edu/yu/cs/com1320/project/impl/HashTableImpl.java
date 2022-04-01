package  edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

// Right now, I have a problem that I changed my file path halfway through and now IntelliJ lost track of the project
// But I can't tell if the problem is in IntelliJ or in Maven, so I don't know what to fix
// Hopefully, tomorrow we will learn Maven and I will learn how to fix the problem
public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {

    private ChainLink[] table; // the table in the HashTable
    private int contentCount; // this says how many items are in the array, since I have no good way of knowing
        // otherwise
    // This is the constructor to create a new HashTable

    public HashTableImpl() {
        // I need to create the array
        // The array will be an array of ChainLinks
        // I will keep the base length of 5, since I was never told to change it
        // I will make a second constructor, private for now, that accepts a constructor of any length
        // Just in case we need that in the future, I can just make it public
        // There shouldn't be a problem of a second constructor if it is private
        this(5);
    }

    private HashTableImpl(int length) {
        table = new ChainLink[length];
        // I feel like there should be something else here, but can't think of it
    }

    // returns a hashcode compatible with my length-5 table
    private int hashFunction(Key k) {
        return Math.abs(k.hashCode()) % table.length;
    }

    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
    @Override
    public Value get(Key k) {
        if (k == null) { // since it isn't in the table, it returns null
            return null;
        }
        int hashValue = hashFunction(k);
        ChainLink link = table[hashValue]; // getting the right link
        while (link != null) {
            if (k.equals(link.k)) {
                return (Value) link.v;
            }
            link = link.nextLink;
        }
        return null;
    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store.
     *          To delete an entry, put a null value.
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    @Override
    public Value put(Key k, Value v) {
        if (k == null) { // this was what Piazza said to do, though I don't think it mattered much
            return null;
        }
        ChainLink link = table[hashFunction(k)]; // getting the right link
        ChainLink previousLink = null; // Has the link before this one, so no problems if
        // I go through the links until I find the right one or we run out of links
        while (link != null) {
            if (k.equals(link.k)) {
                return replaceOldValueWithNew(k, v, link, previousLink);
            }
            previousLink = link;
            link = link.nextLink;
        }
        // I don't want to add a new link with a null value
        if (v == null) { // if the key is not in the table but the value is also null, so it is a bad delete
            return null;
        }
        // if we don't find the key, because link == null, we add a new link to the end
        addNewLink(k, v, previousLink);
        return null; // because we didn't replace anything
    }

    // I made this a private method so I could stop put() from being a monster method
    // It is called if we find a link with the same key during put(), and we replace the old value with the new one, or if the new value is null,
        // we delete the link
    private Value replaceOldValueWithNew(Key k, Value v, ChainLink link, ChainLink previousLink) {
        int hashValue = hashFunction(k);
        Value previous = (Value) link.v;
        if (v != null) {
            link.v = v;
        } else {
            // I was told that I now need to actually remove links from the hashtable instead of just giving them
            // a null value
            if (previousLink == null) {
                table[hashValue] = null;
            } else {
                previousLink.nextLink = link.nextLink;
            }
            contentCount--; // Since it is no longer taking up space
        }
        return previous;
    }

    // adds a new link with Key k and Value v at the end of a predetermined previous link
    private void addNewLink(Key k, Value v, ChainLink previousLink) {
        int hashValue = hashFunction(k);
        ChainLink newLink = new ChainLink(k, v);
        if (previousLink != null) {
            previousLink.nextLink = newLink;
        } else { // if this will be the first link on the chain
            table[hashValue] = newLink; // starting the chain
        }
        contentCount++; // since we just added a new link
        if (contentCount > 0.75 * table.length) { // the recommended threshold
            resizeArray();
        }
    }

    private void resizeArray() {
        // I must store the old table
        ChainLink[] oldTable = table;
        // I must create a new array with double the length
        // this way, put will put into the correct table, and hashFunction will hash correctly
        table = new ChainLink[table.length * 2];
        // I must set the contentCount to 0, because otherwise, I will increase it more than I should
        contentCount = 0;
        // Then I must traverse the old array
        for (ChainLink slot : oldTable) {
            // If the slot isn't null, I must traverse the chainlinks
            if (slot != null) {
                ChainLink link = slot;
                while (link != null) {
                    // I must take each link and add its key-value pair to the new array
                    // This should return the contentCount to its old value, since put() does that
                    put((Key) link.k, (Value) link.v);
                    link = link.nextLink;
                }
            }
        }
        // at this point, everything should be in its new slot, and Java can throw the old table in the trash
    }


    private static class ChainLink {
        // I am going to have each chain contain the key, the value, and the next chain link
        // It turns out private values can be accessed in the outer class, so there is no point in getters
        // or setters here
        // I got rid of the parameterized types, because it was a problem with generics
        // But it shouldn't be a problem for my code, because it already has generics further up
        private Object k; // The key being stored
        private Object v; // The value being stored
        private ChainLink nextLink; // The next chain, null if this is the last chain

        private ChainLink(Object k, Object v) {
            this.k = k;
            this.v = v;
            // nextLink is automatically null
        }

    }
}