package  edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

// Right now, I have a problem that I changed my file path halfway through and now IntelliJ lost track of the project
// But I can't tell if the problem is in IntelliJ or in Maven, so I don't know what to fix
// Hopefully, tomorrow we will learn Maven and I will learn how to fix the problem
public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {

    private ChainLink[] table; // the table in the HashTable
    // This is the constructor to create a new HashTable

    public HashTableImpl() {
        // I need to create the array
        // The array will be an array of ChainLinks
        // It will have a length of 5, as required
        // I will make a second constructor, private for now, that accepts a constructor of any length
        // Just in case we need that in the future, I can just make it public
        this(5);
    }

    private HashTableImpl(int length) {
        table = new ChainLink[length];
        assert table.length == 5;
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
        if (k == null) {
            throw new IllegalArgumentException("null keys are not supported");
        }
        int hashValue = hashFunction(k);
        ChainLink link = table[hashValue]; // getting the right link
        ChainLink previousLink = null; // Has the link before this one, so no problems if
        // I go through the links until I find the right one or we run out of links
        while (link != null) {
            if (k.equals(link.k)) {
                Value previous = (Value) link.v;
                link.v = v;
                return previous;
            }
            previousLink = link;
            link = link.nextLink;
        }
        // if we don't find the key, because link == null, we add a new link to the end
        ChainLink newLink = new ChainLink(k, v);
        if (previousLink != null) {
            previousLink.nextLink = newLink;
        } else { // if this will be the first link on the chain
            table[hashValue] = newLink; // starting the chain
        }
        return null; // because we didn't replace anything
    }


    protected static class ChainLink {
        // I am going to have each chain contain the key, the value, and the next chain link
        // All the values are protected, because since only I have access to it, and I was going to have
        // getters and setters anyway, I am giving maximum freedom
        // Something is probably going to go wrong
        // I got rid of the parameterized types, because it was a problem with generics
        // But it shouldn't be a problem for my code, because it already has generics further up
        protected Object k; // The key being stored
        protected Object v; // The value being stored
        protected ChainLink nextLink; // The next chain, null if this is the last chain

        protected ChainLink(Object k, Object v) {
            this.k = k;
            this.v = v;
            // nextLink is automatically null
        }

    }
}