package  edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

public class HashTableImpl<Key, Value> implements HashTable {

    // This is the constructor to create a new HashTable
    public HashTableImpl() {
        // I need to create the array
        // Before I do anything else, I need to add another file to the package that can act as a chain list
        // In fact, I think I will call it ChainList
    }

    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
    @Override
    public Object get(Object k) {
        return null;
    }

    /**
     * @param k the key at which to store the value
     * @param v the value to store.
     *          To delete an entry, put a null value.
     * @return if the key was already present in the HashTable, return the previous value stored for the key. If the key was not already present, return null.
     */
    @Override
    public Object put(Object k, Object v) {
        return null;
    }

    protected class ChainLink<Key, Value> {
        // I am going to have each chain contain the key, the value, and the next chain link
        // All the values are protected, because since only I have access to it, and I was going to have
        // getters and setters anyway, I am giving maximum freedom
        // Something is probably going to go wrong
        protected Key k; // The key being stored
        protected Value v; // The value being stored
        protected ChainLink nextLink; // The next chain, null if this is the last chain

        protected ChainLink(Key k, Value v) {
            this.k = k;
            this.v = v;
            // nextLink is automatically null
        }

    }
}