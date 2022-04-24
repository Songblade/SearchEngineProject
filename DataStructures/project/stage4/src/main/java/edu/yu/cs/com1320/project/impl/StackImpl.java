package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {

    private StackNode<T> mouth; // where links are added and removed from
    private int size; // measures the current size

    // no argument constructor
    public StackImpl() {
        mouth = new StackNode<>(null); // the sentinel, to make adding and deleting easier
        size = 0;
    }
    /**
     * @param element object to add to the Stack
     */
    @Override
    public void push(T element) {
        if (element == null) { // since null is how I know that this is the sentinel
            throw new IllegalArgumentException("Null elements are not supported");
        }
        // Since we know this is a real element, we up the size
        size++;
        // I create a new link with the element
        StackNode<T> newNode = new StackNode<>(element);
        // I set the current mouth as the new node's nextNode
        newNode.nextNode = mouth;
        // I make the element just added the new mouth
        mouth = newNode;
    }

    /**
     * removes and returns element at the top of the stack
     *
     * @return element at the top of the stack, null if the stack is empty
     */
    @Override
    public T pop() {
        if (mouth.value == null) {
            return null; // without actually removing anything
        }
        // If there is something to be removed
        size--; // Since we know we are actually removing something, we decrease the size
        T returnValue = mouth.value;
        mouth = mouth.nextNode; // we remove the node and replace it with what comes next
        // this won't cause any errors, because if this is the sentinel, it will return null without removing anything
        return returnValue;
    }

    /**
     * @return the element at the top of the stack without removing it
     */
    @Override
    public T peek() {
        return mouth.value;
    }

    /**
     * @return how many elements are currently in the stack
     */
    @Override
    public int size() {
        return size;
    }

    private static class StackNode<T> {
        // This is mostly just the ChainLink from HashTable, but without its second value
        private T value; // The key being stored
        private StackNode<T> nextNode; // The next chain, null if this is the last chain

        private StackNode(T value) {
            this.value = value;
            // nextLink is automatically null
        }

    }
}