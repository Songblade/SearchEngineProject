package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.IOException;
import java.net.URI;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {

    private static final int MAX = 4;
    private Node<Key, Value> root; //root of the B-tree
    private int height; //height of the B-tree
    private int n; //number of key-value pairs in the B-tree
    private PersistenceManager<Key, Value> memory;

    //B-tree node data type
    private static final class Node<Key extends Comparable<Key>, Value> {
        private int entryCount; // number of entries
        private Entry<Key, Value>[] entries = (Entry<Key, Value>[]) new Entry[MAX]; // the array of children
        private Node<Key, Value> next;
        // create a node with k entries
        private Node(int k)
        {
            this.entryCount = k;
        }
    }

    //internal nodes: only use key and child
    //external nodes: only use key and value
    private static class Entry<Key extends Comparable<Key>, Value> {
        private Key key;
        private Value val;
        private Node<Key, Value> child;
        private boolean isInDisk; // if false, object is in Object. If not, object is at the Key in memory

        // constructor for branch node
        private Entry(Key key, Node<Key, Value> child) {
            this.key = key;
            this.child = child;
        }

        // constructor for leaf node when value is here
        private Entry(Key key, Value val) {
            this.key = key;
            this.val = val;
        }

        // constructor for leaf node when value is missing
        private Entry(Key key) {
            this.key = key;
            this.isInDisk = true;
        }

    }

    /**
     * Initializes an empty B-tree.
     */
    public BTreeImpl() {
        this.root = new Node<>(0);
    }


    @Override
    public Value get(Key k) {
        if (k == null) {
            throw new IllegalArgumentException("argument to get() is null");
        }
        Entry<Key, Value> entry = this.get(this.root, k, this.height);
        if (entry == null) {
            return null;
        } else if (entry.isInDisk) { // means on the disk
            try {
                Value returnValue = memory.deserialize(k);
                memory.delete(k);
                entry.isInDisk = false; // since we just retrieved it
                return returnValue;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

        } else { // if value is not in the disk
            return entry.val;
        }
    }

    private Entry<Key, Value> get(Node<Key, Value> currentNode, Key key, int height) {
        Entry<Key, Value>[] entries = currentNode.entries;

        //current node is external (i.e. height == 0)
        if (height == 0) {
            for (int j = 0; j < currentNode.entryCount; j++) {
                if(isEqual(key, entries[j].key)) {
                    //found desired key. Return its value
                    return entries[j];
                }
            }
            //didn't find the key
            return null;
        } else {  //current node is internal (height > 0)
            for (int j = 0; j < currentNode.entryCount; j++) {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be in the subtree below the current entry),
                //then recurse into the current entry’s child
                if (j + 1 == currentNode.entryCount || less(key, entries[j + 1].key)) {
                    return this.get(entries[j].child, key, height - 1);
                }
            }
            //didn't find the key
            return null;
        }
    }

    // comparison functions - make Comparable instead of Key to avoid casts
    private boolean less(Key k1, Key k2) {
        return k1.compareTo(k2) < 0;
    }

    private boolean isEqual(Key k1, Key k2) {
        return k1.compareTo(k2) == 0;
    }

    private int getSize() { // this method is unnecessary, but I have this prepared, so it feels like a waste not to have it
        return n;
    }

    @Override
    public Value put(Key k, Value v) {
        if (k == null) {
            throw new IllegalArgumentException("argument key to put() is null");
        }
        //if the key already exists in the b-tree, simply replace the value
        Entry<Key, Value> alreadyThere = this.get(this.root, k, this.height);
        if(alreadyThere != null) {
            if (alreadyThere.isInDisk) { // if there is a previous thing in the disk
                try {
                    memory.delete(k);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
            Value returnVal = alreadyThere.val;
            alreadyThere.val = v;
            alreadyThere.isInDisk = false; //just in case, since now it is in local memory
            return returnVal;
        }

        // if the key does not yet exist in the B-Tree
        Node<Key, Value> newNode = this.put(this.root, k, v, this.height);
        this.n++;
        if (newNode == null) { // if we don't have to split the root
            return null; // since this is a new put, not replacing anything
        }

        //split the root:
        //Create a new node to be the root.
        //Set the old root to be new root's first entry.
        //Set the node returned from the call to put to be new root's second entry
        Node<Key, Value> newRoot = new Node<>(2);
        newRoot.entries[0] = new Entry<>(this.root.entries[0].key, this.root);
        newRoot.entries[1] = new Entry<>(newNode.entries[0].key, newNode);
        this.root = newRoot;
        //a split at the root always increases the tree height by 1
        this.height++;
        return null; // because we didn't replace anything
    }

    /**
     *
     * @param currentNode we are looking at
     * @param key that we are looking for
     * @param val that we want to insert
     * @param height that we are currently at, where root is considered the max height
     * @return null if no new node was created (i.e. just added a new Entry into an existing node). If a new node was created due to the need to split, returns the new node
     */
    private Node<Key, Value> put(Node<Key, Value> currentNode, Key key, Value val, int height)
    {
        int j;
        Entry<Key, Value> newEntry = new Entry<>(key, val);

        //external node
        if (height == 0) {
            //find index in currentNode’s entry[] to insert new entry
            //we look for key < entry.key since we want to leave j
            //pointing to the slot to insert the new entry, hence we want to find
            //the first entry in the current node that key is LESS THAN
            for (j = 0; j < currentNode.entryCount; j++) {
                if (less(key, currentNode.entries[j].key)) {
                    break;
                }
            }
        } else { // if this is an internal node, and we need to find the next node
            //find index in node entry array to insert the new entry
            for (j = 0; j < currentNode.entryCount; j++) {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be added to the subtree below the current entry),
                //then do a recursive call to put on the current entry’s child
                if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key)) {
                    //increment j (j++) after the call so that a new entry created by a split
                    //will be inserted in the next slot
                    Node<Key, Value> newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);
                    if (newNode == null) {
                        return null;
                    }
                    //if the call to put returned a node, it means I need to add a new entry to
                    //the current node
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }
        //shift entries over one place to make room for new entry
        for (int i = currentNode.entryCount; i > j; i--) {
            currentNode.entries[i] = currentNode.entries[i - 1];
        }
        //add new entry
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;
        if (currentNode.entryCount < MAX) {
            //no structural changes needed in the tree
            //so just return null
            return null;
        }
        else {
            //will have to create new entry in the parent due
            //to the split, so return the new node, which is
            //the node for which the new entry will be created
            return this.split(currentNode, height);
        }
    }

    /**
     * split node in half
     * @param currentNode we are splitting
     * @return new node
     */
    private Node<Key, Value> split(Node<Key, Value> currentNode, int height)
    {
        Node<Key, Value> newNode = new Node<>(MAX / 2);
        //by changing currentNode.entryCount, we will treat any value
        //at index higher than the new currentNode.entryCount as if
        //it doesn't exist
        currentNode.entryCount = MAX / 2;
        //copy top half of h into t
        for (int j = 0; j < MAX / 2; j++) {
            newNode.entries[j] = currentNode.entries[MAX / 2 + j];
        }
        //external node
        if (height == 0) {
            newNode.next = currentNode.next;
            currentNode.next = newNode;
        }
        return newNode;
    }

    @Override
    public void moveToDisk(Key k) throws Exception {
        if (k == null) {
            throw new IllegalArgumentException("key is null");
        }
        Value value = get(k);
        if (value == null) {
            throw new IllegalStateException("Value is not in memory");
        }
        // what I need to do is first put this in the persistence manager
        memory.serialize(k, value);
        // then I need to replace its node in the B-Tree with a reference node
        changeNodeToReference(root, k, this.height);
        // then I need to add to get that if it reaches a reference node, to retrieve it from
            // memory and delete it in memory
        // then I need to add to put that if it reaches a reference node, to delete the memory
            // before putting it here
    }

    /**
     *
     * @param currentNode we are looking at
     * @param key that we are looking for
     * @param height that we are currently at, where root is considered the max height
     */
    private void changeNodeToReference(Node<Key, Value> currentNode, Key key, int height) {
        int j;

        //leaf node
        if (height == 0) {
            //find index in currentNode’s entry[] to make entry reference
            //we look for key < entry.key since we want to leave j
            //pointing to the slot to insert the new entry, hence we want to find
            //the first entry in the current node that key is LESS THAN
            for (j = 0; j < currentNode.entryCount; j++) {
                if (isEqual(key, currentNode.entries[j].key)) {
                    currentNode.entries[j] = new Entry<>(key); // change to reference node
                    return;
                }
            }

        } else { // if this is an internal node, and we need to find the next node
            //find index in node entry array to insert the new entry
            for (j = 0; j < currentNode.entryCount; j++) {
                //if (we are at the last key in this node OR the key we
                //are looking for is less than the next key, i.e. the
                //desired key must be added to the subtree below the current entry),
                //then do a recursive call to put on the current entry’s child
                if ((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key)) {
                    //increment j (j++) after the call so that a new entry created by a split
                    //will be inserted in the next slot
                    changeNodeToReference(currentNode.entries[j].child, key, height);
                    return;
                }
            }
        }
    }

    @Override
    public void setPersistenceManager(PersistenceManager<Key, Value> pm) {
        memory = pm;
    }
}
