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
        if (val == null) {
            throw new IllegalArgumentException("Value is null");
        }
        String newKey = cleanKey(key);
        if (newKey.isEmpty()) {
            return;
        }
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
        if (key == null) {
            throw new IllegalArgumentException("Key is null");
        }
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
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator is null");
        }
        String cleanedKey = cleanKey(key);
        if (cleanedKey.isEmpty()) {
            return new ArrayList<>();
        }
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
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator is null");
        }
        String cleanedPrefix = cleanKey(prefix);
        if (cleanedPrefix.isEmpty()) {
            return new ArrayList<>();
        }
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
        String cleanedPrefix = cleanKey(prefix);
        if (cleanedPrefix.isEmpty()) {
            return new HashSet<>();
        }
        // first I need to get the prefix
        Node<Value> prefixNode = get(root, cleanedPrefix);
        // then I need to get a set of values that are in and below that prefix
        if (prefixNode == null) {
            return new HashSet<>(); // if the prefix node isn't there, there is nothing to delete
        }
        Set<Value> deleted = getPrefixSet(prefixNode);
        // then I need to delete the prefix
        deleteNode(root, cleanedPrefix);
        // then I need to return the values
        return deleted;
    }

    // deletes a node and all of its children, and maybe parents if they don't have any other children
    private boolean deleteNode(Node<Value> currentNode, String key) {

        char c = key.charAt(0);
        int cValue = getArrayValue(c); // the value of c's array slot

        // if we've reached the last node in the key, delete the node

        if (key.length() == 1) {
            currentNode.links[cValue] = null; // deleting the value
            return false; // time to go up, we return false since we may or may not delete its parent also
        }

        //proceed to the next node in the chain of nodes that forms the desired key
        key = key.substring(1); // this must be after the stopping point, so that we have the right key length

        // next link is null, means what we are deleting is already deleted, time to go up
        if (currentNode.links[cValue] == null) {
            return true; // The only way possible to have a null value is if there is another brother of the node
                // that does have children
        }
        //proceed to the next node in the chain of nodes that forms the desired key
        boolean knownChildren = this.deleteNode(currentNode.links[cValue], key);
        // now we need to make sure that if there aren't any other children, we delete the parent node as well
        // if knownChildren is false, we check if it has other children, and if it does, we delete them
        // if it doesn't, we return true
        // if we got true, we return true
        if (knownChildren) {
            return true;
        } else {
            int numOfChildren = childNumber(currentNode);
            if (numOfChildren > 1) {
                // we can't delete this node or any others, because there are other children
                return true;
            } else if (numOfChildren == 1) {
                // if we went through the entire loop without finding any other non-null children, they must not exist
                // so we can delete this value too, and tell the parent to check the children also
                currentNode.links[cValue] = null;
                return false;
            } else {
                throw new IllegalStateException("Somehow, you have a parent with no children");
            }
        }
    }

    private int childNumber(Node<Value> node) {
        int childNumber = 0;
        // I can't use a for-each loop, because I can't delete a node in the middle of it
        for (int child = 0; child < alphabetSize; child++) {
            if (node.links[child] != null) {
                childNumber++;
            }
        }
        return childNumber;
    }

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     *
     * @param key
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAll(String key) {
        String cleanedKey = cleanKey(key);
        if (cleanedKey.isEmpty()) {
            return new HashSet<>();
        }
        // first, we get the node
        Node<Value> node = get(root, cleanedKey);
        if (node == null) {
            return new HashSet<>(); // if the prefix node isn't there, there is nothing to delete
        }
        // then, we check if it has children
        if (childNumber(node) == 0) {
            // if it has no children, we call the deleteAllWithPrefix method and return that, so that it
                // can deal with the going up and deleting
            return deleteAllWithPrefix(cleanedKey);
        }
        // if it has children, we don't want to delete the node, so we extract its values and delete them
        Set<Value> deleted = node.values;
        node.values = new HashSet<>(); // deletes the values
        return deleted;
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
        // first, we clean the key and get the node
        String cleanedKey = cleanKey(key);
        if (cleanedKey.isEmpty()) {
            return null;
        }
        if (val == null) {
            throw new IllegalArgumentException("Value is null");
        }
        Node<Value> node = get(root, cleanedKey);
        // if the value isn't there, we return null
        if (node == null || !node.values.contains(val)) {
            return null; // if the prefix node isn't there, there is nothing to delete
        }
        // if this is the only value in the node, we return the value from the set from deleteAll
        if (node.values.size() == 1) {
            Set<Value> deleted = deleteAll(cleanedKey);
            if (deleted.size() != 1) {
                throw new IllegalStateException("deleted is wrong size");
            }
            if (!deleted.contains(val)) {
                throw new IllegalStateException("deleted doesn't contain value");
            }
            return val;
        }
        // if there are other values, we delete the value and return it
        node.values.remove(val);
        return val;
    }
}