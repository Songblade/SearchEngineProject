package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {
    @Override
    public Value get(Key k) {
        return null;
    }

    @Override
    public Value put(Key k, Value v) {
        return null;
    }

    @Override
    public void moveToDisk(Key k) throws Exception {

    }

    @Override
    public void setPersistenceManager(PersistenceManager<Key, Value> pm) {

    }
}
