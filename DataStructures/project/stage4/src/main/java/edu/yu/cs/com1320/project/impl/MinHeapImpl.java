package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;

import java.util.Arrays;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {

    public MinHeapImpl() {
        elements = (E[]) new Comparable[16]; // completely random number, since I don't have a better idea
    }

    @Override
    public void reHeapify(E element) {
        // finds the location of the element, in only O(n) time, unfortunately
        int elementIndex = getArrayIndex(element);
        // upheaps and downheaps, only one (or maybe zero) will actually do anything
        upHeap(elementIndex);
        downHeap(elementIndex);
    }

    @Override
    protected int getArrayIndex(E element) {
        // in order to get the Array index, the only thing that makes sense is to traverse the array linearly
        // Since the Array isn't sorted fully, you can't use binary sort
        // So this method has O(n) time
        for (int index = 1; index < elements.length; index++) {
            // we start at index 1, because 0 here is unused
            if (elements[index] == null) { // we have reached the end of the heap that has stuff
                return -1;
            }
            if (elements[index].equals(element)) {
                return index;
            }
        }
        return -1; // only applies if the heap is about to be doubled
    }

    @Override
    protected void doubleArraySize() {
        // copies all old elements into the new array, which is double the length
        // assuming I correctly understand what this is for, this was easy, it's like Python
        elements = Arrays.copyOf(elements, elements.length * 2);
    }
}
