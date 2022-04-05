package edu.yu.cs.com1320.project.stage3;

import edu.yu.cs.com1320.project.Trie;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import edu.yu.cs.com1320.project.impl.TrieImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class TrieTest {

    // Tests for put:
    // Test that add works, even when add a whole bunch of words to a whole bunch of keys
    // Also tests that works even if you add a word and a larger word, like a1 and a10 or a11
    @Test
    public void testPutLargeDifKey() {
        Trie<Integer> trie = new TrieImpl<>();
        for (int i = 0; i < 1000000; i++) {
            trie.put("a" + i, i); // I want to make sure it works for more than just numbers
        }
        for (int i = 0; i < 1000000; i++) {
            ArrayList<Integer> result = new ArrayList<>();
            result.add(i);
            assertEquals(result, trie.getAllSorted("a" + i, Comparator.naturalOrder()));
        }
        for (int i = 999999; i >= 0; i--) {
            ArrayList<Integer> result = new ArrayList<>();
            result.add(i);
            assertEquals(result, trie.getAllSorted("a" + i, Comparator.naturalOrder()));
        }
    }

    // Test that add works, even when add a whole bunch of words to the same key

    @Test
    public void testPutLargeSameKey() {
        Trie<Integer> trie = new TrieImpl<>();
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            trie.put("alphabetKey", i); // I want to make sure it works for more than just numbers
            result.add(i);
        }
        result.sort(Comparator.naturalOrder());
        assertEquals(result, trie.getAllSorted("alphabetKey", Comparator.naturalOrder()));
    }

    // Makes sure that adding the same value twice to the same key only results in the value added once
    // And that can still add the same value to another key
    @Test
    public void testPutAddsOnce() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("Yo", 17);
        trie.put("Hi", 17);
        trie.put("Yo", 17);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        assertEquals(result, trie.getAllSorted("Yo", Comparator.naturalOrder()));
        assertEquals(result, trie.getAllSorted("Hi", Comparator.naturalOrder()));
    }

    // Test that works for multiple data types
    @Test
    public void testWorksMultiType() {
        Trie<String> trie = new TrieImpl<>();
        trie.put("Yo", "Dude");
        trie.put("Hi", "Good");
        trie.put("Yo", "Food");
        ArrayList<String> result = new ArrayList<>();
        result.add("Dude");
        result.add("Food");
        result.sort(Comparator.naturalOrder());
        assertEquals(result, trie.getAllSorted("Yo", Comparator.naturalOrder()));
        //Reusing result, now it is the result of searching up "Hi"
        result = new ArrayList<>();
        result.add("Good");
        assertEquals(result, trie.getAllSorted("Hi", Comparator.naturalOrder()));
    }

    // Test that put ignores all non-alphanumerics
    @Test
    public void putOnlyAddsLetters() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You!@#$%^&*()_+`~_+[]{}\\|;:'\"<>,.?/Tube", 17);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        assertEquals(result, trie.getAllSorted("YouTube", Comparator.naturalOrder()));
    }

    // Test that put ignores 1 non-alphanumeric at the end
    @Test
    public void putIgnoresSymbolAtEnd() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You!Tube!", 17);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        assertEquals(result, trie.getAllSorted("YouTube", Comparator.naturalOrder()));
    }

    // Test that put ignores many non-alphanumerics at the end
    @Test
    public void putIgnoresSymbolsAtEnd() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You!Tube!!", 17);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        assertEquals(result, trie.getAllSorted("YouTube", Comparator.naturalOrder()));
    }

    // making sure some methods ignore blank calls
    @Test
    public void putIgnoresBlanks() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("", 17);
        trie.put("__)(", 17);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        assertEquals(result, trie.getAllSorted("", Comparator.naturalOrder()));
    }



    // Tests for getAllSorted:
    // Test that makes sure get returns in order, even when not added in that order
    @Test
    public void getAllSortedSorts() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("Yo", 17);
        trie.put("Yo", 15);
        trie.put("Yo", 16);
        ArrayList<Integer> result = new ArrayList<>(List.of(15,16,17));
        result.sort(Comparator.naturalOrder());
        assertEquals(result, trie.getAllSorted("Yo", Comparator.naturalOrder()));
    }

    // Test that can use different comparators on the same data set to get different orders
    @Test
    public void getAllSortedUsesComparator() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("Yo", 17);
        trie.put("Yo", 15);
        trie.put("Yo", 16);
        ArrayList<Integer> result = new ArrayList<>(List.of(15,16,17));
        result.sort(Comparator.naturalOrder());
        assertEquals(result, trie.getAllSorted("Yo", Comparator.naturalOrder()));
        result.sort((Comparator<Integer>) Comparator.naturalOrder().reversed());
        assertEquals(result, trie.getAllSorted("Yo", (Comparator<Integer>) Comparator.naturalOrder().reversed()));
    }

    // Test that makes sure gives empty set if the word isn't there but a longer word is
    @Test
    public void getAllSortedIgnoresChildren() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("Yo", 17);
        ArrayList<Integer> result = new ArrayList<>();
        assertEquals(result, trie.getAllSorted("Y", Comparator.naturalOrder()));
        result.add(17);
        assertEquals(result, trie.getAllSorted("Yo", Comparator.naturalOrder()));
    }

    // Test that makes sure gives empty set if word isn't there, nor is a longer one
    @Test
    public void getAllSortedReturnsEmpty() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("Yo", 17);
        ArrayList<Integer> result = new ArrayList<>();
        assertEquals(result, trie.getAllSorted("Yoo", Comparator.naturalOrder()));
        assertEquals(result, trie.getAllSorted("HiThere", Comparator.naturalOrder()));
        result.add(17);
        assertEquals(result, trie.getAllSorted("Yo", Comparator.naturalOrder()));
    }

    // Test that makes sure case-insensitive
    @Test
    public void getAllSortedCaseIgnorant() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("Yo", 17);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        assertEquals(result, trie.getAllSorted("YO", Comparator.naturalOrder()));
        assertEquals(result, trie.getAllSorted("yO", Comparator.naturalOrder()));
        assertEquals(result, trie.getAllSorted("Yo", Comparator.naturalOrder()));
        assertEquals(result, trie.getAllSorted("yo", Comparator.naturalOrder()));
    }

    // Test that put ignores all non-alphanumerics
    @Test
    public void getAllSortedOnlyAddsLetters() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("YouTube", 17);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        assertEquals(result, trie.getAllSorted("You!@#$%^&*()_+`~_+[]{}\\|;:'\"<>,.?/Tube", Comparator.naturalOrder()));
    }

    // Test that put ignores 1 non-alphanumeric at the end
    @Test
    public void getAllSortedIgnoresSymbolAtEnd() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("YouTube", 17);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        assertEquals(result, trie.getAllSorted("You!Tube!", Comparator.naturalOrder()));
    }

    // Test that put ignores many non-alphanumerics at the end
    @Test
    public void getAllSortedIgnoresSymbolsAtEnd() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("YouTube", 17);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        assertEquals(result, trie.getAllSorted("You!Tube!!", Comparator.naturalOrder()));
    }

    // Tests for getAllWithPrefixSorted:
    // Test that makes sure works when nothing there but the prefix
    @Test
    public void getAllWithPrefixSortedWorksWithPrefix() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You", 17);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        assertEquals(result, trie.getAllWithPrefixSorted("You", Comparator.naturalOrder()));
    }

    // Test that makes sure works when nothing there but longer words
    @Test
    public void getAllWithPrefixSortedWorksWithSuffixes() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("YouTube", 18);
        trie.put("YouTuber", 19);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(18);
        result.add(19);
        result.sort(Comparator.naturalOrder());
        assertEquals(result, trie.getAllWithPrefixSorted("You", Comparator.naturalOrder()));
    }

    // Test that makes sure works when are values here and with suffixes
    @Test
    public void getAllWithPrefixSortedWorksWithPreAndSuf() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You", 17);
        trie.put("YouTube", 18);
        trie.put("YouTuber", 19);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        result.add(18);
        result.add(19);
        result.sort(Comparator.naturalOrder());
        assertEquals(result, trie.getAllWithPrefixSorted("You", Comparator.naturalOrder()));
    }

    // Test that makes sure works when have many values at same level
    @Test
    public void getAllWithPrefixSortedWorksWide() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("YouTube", 17);
        trie.put("YouTub3", 18);
        trie.put("YouTub7", 19);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        result.add(18);
        result.add(19);
        result.sort(Comparator.naturalOrder());
        assertEquals(result, trie.getAllWithPrefixSorted("You", Comparator.naturalOrder()));
    }

    // Makes sure that if the same value is under 2 words, it only shows up once
    @Test
    public void getAllWithPrefixSortedCountsWordsOnce() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("YouTube", 18);
        trie.put("YouTube2022", 18);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(18);
        assertEquals(result, trie.getAllWithPrefixSorted("You", Comparator.naturalOrder()));
    }

    // Test that can use different comparators on the same data set to get different orders
    @Test
    public void getAllWithPrefixSortedWorksWithDifComparators() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You", 17);
        trie.put("YouTube", 18);
        trie.put("YouTuber", 19);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        result.add(18);
        result.add(19);
        result.sort(Comparator.naturalOrder());
        assertEquals(result, trie.getAllWithPrefixSorted("You", Comparator.naturalOrder()));
        result.sort((Comparator<Integer>) Comparator.naturalOrder().reversed());
        assertEquals(result, trie.getAllWithPrefixSorted("Yo", (Comparator<Integer>) Comparator.naturalOrder().reversed()));
    }

    // Test that makes sure empty set if nothing there
    @Test
    public void getAllWithPrefixSortedEmpty() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("Yowzers2", 17);
        ArrayList<Integer> result = new ArrayList<>();
        assertEquals(result, trie.getAllWithPrefixSorted("You", Comparator.naturalOrder()));
    }

    // Test that makes sure case-insensitive
    // Test that ignores symbols
    @Test
    public void getAllWithPrefixSortedIsClean() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You!@#$%^&*()_+`~_+[]{}\\|;:'\"<>,.?/Tube", 17);
        trie.put("You!Tube!!", 18);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        result.add(18);
        result.sort(Comparator.naturalOrder());
        assertEquals(result, trie.getAllSorted("youtube", Comparator.naturalOrder()));
    }

    // Tests for deleteAllWithPrefix:

    // Test makes sure suffixes actually deleted, and everything in this node
    @Test
    public void deleteAllWithPrefixWorksNarrow() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You", 17);
        trie.put("YouTube", 18);
        trie.put("YouTuber", 19);
        trie.deleteAllWithPrefix("You");
        ArrayList<Integer> result = new ArrayList<>();
        assertEquals(result, trie.getAllWithPrefixSorted("You", Comparator.naturalOrder()));
    }
    
    // Makes sure Set is returned containing everything that was deleted
    // Makes sure empty set returned if nothing deleted
    // Makes sure empty set returned if prefix isn't in tree
    // Test that makes sure case-insensitive and ignores symbols

    // Tests for deleteAll:
    // Makes sure deletes all values
    // Makes sure does NOT delete suffix values
    // Makes sure Set is returned containing everything that was deleted
    // Makes sure empty set returned if nothing deleted
    // Makes sure empty set returned if isn't in tree
    // Test that makes sure case-insensitive and ignores symbols

    // Tests for delete:
    // Makes sure deletes the value
    // Makes sure does not delete other values or suffix values
    // Makes sure returns deleted value
    // Makes sure returns null if nothing deleted, only suffixes, or not in tree
    // Test that makes sure case-insensitive and ignores symbols


}