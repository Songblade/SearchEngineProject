package  edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;

// Right now, I have a problem that I changed my file path halfway through and now IntelliJ lost track of the project
// But I can't tell if the problem is in IntelliJ or in Maven, so I don't know what to fix
// Hopefully, tomorrow we will learn Maven and I will learn how to fix the problem
public class HashTableImpl<Key, Value> implements HashTable {

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
        // I feel like there should be something else here, but can't think of it
    }

    /**
     * @param k the key whose value should be returned
     * @return the value that is stored in the HashTable for k, or null if there is no such key in the table
     */
    @Override
    public Value get(Key k) {
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
        ChainLink link = table[]
        return null;
    }

    // returns a hashcode compatible with my length-5 table
    private int hashFunction(Key k) {
        return Math.abs(k.hashCode()) % table.length;
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