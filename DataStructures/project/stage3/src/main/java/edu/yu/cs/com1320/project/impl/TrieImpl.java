package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {

    private static final int alphabetSize = 36; // alphanumerics
    private Node<Value> root; // root of trie

    private static class Node<Value> {
        private Set<Value> values; // a set so that can't have duplicates
        private Node<Value>[] links = new Node[alphabetSize];

        private Node() {
            values = new HashSet<>();
        }
    }

    // the null constructor that is required
    public TrieImpl() {
        root = new Node<>();
    }

    /**
     * add the given value at the given key
     *
     * @param key
     * @param val
     */
    @Override
    public void put(String key, Value val) {
        String newKey = cleanKey(key);
        put(this.root, newKey, val);
    }

    private void put(Node<Value> currentNode, String key, Value val) {
        //we've reached the last node in the key,
        //set the value for the key and return the node
        if (key.length() == 0) {
            currentNode.values.add(val);
            return;
        }
        //proceed to the next node in the chain of nodes that forms the desired key
        // I always check the first letter, and then chop it off, so that the first letter is always the right one
        // I don't have to worry about the char not being alphanumeric, because I already made sure of that
            // in the public method
        char c = key.charAt(0);
        key = key.substring(1);
        int cValue = getArrayValue(c); // the value of c's array slot
        // if the node is null, we make a new one
        if (currentNode.links[cValue] == null) {
            currentNode.links[cValue] = new Node<>();
        }
        this.put(currentNode.links[cValue], key, val);
    }

    // this method turns the key lowercase and gets rid of any symbols
    private String cleanKey(String key) {
        String newKey = "";
        // the following loop makes sure the key only contains alphanumerics
        for (int i = 0; i < key.length(); i++) {
            if (Character.getType(key.charAt(i)) == Character.UPPERCASE_LETTER) {
                newKey += (Character.toLowerCase(key.charAt(i)));
                // so that it will be lowercase
            }
            if (Character.getType(key.charAt(i)) == Character.LOWERCASE_LETTER ||
                    Character.getType(key.charAt(i)) == Character.DECIMAL_DIGIT_NUMBER) {
                newKey += (Character.toLowerCase(key.charAt(i)));
            }
            // if it isn't a letter or a number, it isn't added
        }
        return newKey;
    }

    // gets the value of the char's slot in the array
    private int getArrayValue(char c) {
        int value;
        if (c >= 48 && c <= 57) {
            value = c - 48;
        } else if (c >= 97 && c <= 122){
            value = c - 87;
        } else {
            throw new IllegalArgumentException("\"" + c + "\" is not alphanumeric");
        }
        return value;
    }

    /**
     * get all exact matches for the given key, sorted in descending order.
     * Search is CASE INSENSITIVE.
     *
     * @param key
     * @param comparator used to sort  values
     * @return a List of matching Values, in descending order
     */
    @Override
    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {
        String cleanedKey = cleanKey(key);
        Node<Value> node = this.get(this.root, cleanedKey);
        if (node == null) {
            return new ArrayList<>();
        }
        // turning the set into a list
        List<Value> result = new ArrayList<>(node.values);
        result.sort(comparator); // since we are allowed to use lists here, I used the list's sort method
        return result;
    }

    // helper method to recurse down and get node
    private Node<Value> get(Node<Value> currentNode, String key) {
        //we've reached the last node in the key,
        //return the node
        if (key.length() == 0) {
            return currentNode;
        }
        //proceed to the next node in the chain of nodes that forms the desired key
        char c = key.charAt(0);
        key = key.substring(1);
        int cValue = getArrayValue(c); // the value of c's array slot
        // next link is null - return null, indicating a miss
        if (currentNode.links[cValue] == null) {
            return null;
        }
        //proceed to the next node in the chain of nodes that forms the desired key
        return this.get(currentNode.links[cValue], key);
    }

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE INSENSITIVE.
     *
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order
     */
    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {
        String cleanedPrefix = cleanKey(prefix);
        Node<Value> prefixNode = get(root, cleanedPrefix);
        if (prefixNode == null) {
            return new ArrayList<>();
        }
        // Here is where I call the recursive method
        Set<Value> prefixSet = getPrefixSet(prefixNode);
        // turning the set into a list
        List<Value> result = new ArrayList<>(prefixSet);
        result.sort(comparator); // since we are allowed to use lists here, I used the list's sort method
        return result;
    }

    // this gets a set containing all the prefixes
    private Set<Value> getPrefixSet(Node<Value> prefixNode) {
        // Since I use a set, I shouldn't have any duplicate values
        Set<Value> prefixSet = new HashSet<>();
        if (prefixNode.values != null) {
            // adding this node's values
            prefixSet.addAll(prefixNode.values);
        }
        for (Node<Value> node : prefixNode.links) {
            if (node != null) {
                // adding the node's children's values
                prefixSet.addAll(getPrefixSet(node));
            }
        }
        return prefixSet;
    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE INSENSITIVE.
     *
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {
        return null;
    }

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     *
     * @param key
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAll(String key) {
        return null;
    }

    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     *
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    @Override
    public Value delete(String key, Value val) {
        return null;
    }
}