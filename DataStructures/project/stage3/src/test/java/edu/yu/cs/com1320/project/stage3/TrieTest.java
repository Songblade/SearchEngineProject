package edu.yu.cs.com1320.project.stage3;

import edu.yu.cs.com1320.project.Trie;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import edu.yu.cs.com1320.project.impl.TrieImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Comparator;

public class TrieTest {

    public TrieTest(){}

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
    public void deleteAllWithPrefixWorks() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You", 17);
        trie.put("YouTube", 18);
        trie.put("YouTuber", 19);
        trie.put("YouViewer", 20);
        trie.deleteAllWithPrefix("You");
        ArrayList<Integer> result = new ArrayList<>();
        assertEquals(result, trie.getAllWithPrefixSorted("You", Comparator.naturalOrder()));
    }

    // Makes sure Set is returned containing everything that was deleted
    @Test
    public void deleteAllWithPrefixReturns() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You", 17);
        trie.put("YouTube", 18);
        trie.put("YouTube", 19);
        trie.put("YouViewer", 20);
        HashSet<Integer> result = new HashSet<>();
        result.add(17);
        result.add(18);
        result.add(19);
        result.add(20);
        assertEquals(result, trie.deleteAllWithPrefix("You"));
    }

    // Makes sure empty set returned if nothing deleted
    @Test
    public void deleteAllWithPrefixReturnsEmpty() {
        Trie<Integer> trie = new TrieImpl<>();
        HashSet<Integer> result = new HashSet<>();
        assertEquals(result, trie.deleteAllWithPrefix("They"));
        trie.put("You", 17);
        trie.put("YouTube", 18);
        trie.put("YouTuber", 19);
        trie.put("YouViewer", 20);
        assertEquals(result, trie.deleteAllWithPrefix("They"));
    }

    // Test that makes sure case-insensitive and ignores symbols
    @Test
    public void deleteAllWithPrefixIsClean() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You!@#$%^&*()_+`~_+[]{}\\|;:'\"<>,.?/Tube", 17);
        trie.put("You!Tuber!!", 18);
        HashSet<Integer> result = new HashSet<>();
        result.add(17);
        result.add(18);
        assertEquals(result, trie.deleteAllWithPrefix("you"));
        assertEquals(new ArrayList<>(), trie.getAllWithPrefixSorted("You", Comparator.naturalOrder()));
    }

    // makes sure deleting "!" won't delete the entire trie
    // Makes sure Set is returned containing everything that was deleted
    @Test
    public void deleteAllWithPrefixIgnoresSymbolWords() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You", 17);
        trie.put("YouTube", 18);
        trie.put("YouTube", 19);
        trie.put("YouViewer", 20);
        trie.put("!@#$R%", 21);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        result.add(18);
        result.add(19);
        result.add(20);
        assertEquals(new HashSet<>(), trie.deleteAllWithPrefix("!"));
        assertEquals(result, trie.getAllWithPrefixSorted("Y", Comparator.naturalOrder()));
    }

    // Tests for deleteAll:
    // Makes sure deletes all values
    @Test
    public void deleteAllDeletes() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("YouTube", 18);
        trie.put("YouTube", 19);
        trie.deleteAll("YouTube");
        ArrayList<Integer> result = new ArrayList<>();
        assertEquals(result, trie.getAllSorted("YouTube", Comparator.naturalOrder()));
    }

    // Makes sure does NOT delete suffix values
    @Test
    public void deleteAllIgnoresSuffixes() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You", 17);
        trie.put("YouTube", 18);
        trie.put("YouTube", 19);
        trie.put("YouTuber", 20);
        trie.deleteAll("YouTube");
        ArrayList<Integer> result = new ArrayList<>();
        result.add(17);
        result.add(20);
        assertEquals(result, trie.getAllWithPrefixSorted("You", Comparator.naturalOrder()));
    }

    // Makes sure can delete and then add again
    @Test
    public void deleteAllCanAddAgain() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("YouTube", 18);
        trie.put("YouTube", 19);
        trie.deleteAll("YouTube");
        trie.put("YouTube", 18);
        trie.put("YouTube", 20);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(18);
        result.add(20);
        assertEquals(result, trie.getAllSorted("YouTube", Comparator.naturalOrder()));
    }

    // Makes sure Set is returned containing everything that was deleted
    @Test
    public void deleteAllReturns() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You", 17);
        trie.put("YouTube", 18);
        trie.put("YouTube", 19);
        trie.put("YouViewer", 20);
        HashSet<Integer> result = new HashSet<>();
        result.add(18);
        result.add(19);
        assertEquals(result, trie.deleteAll("YouTube"));
    }

    // Makes sure empty set returned if nothing deleted
    // Makes sure empty set returned if isn't in tree
    @Test
    public void deleteAllReturnsEmpty() {
        Trie<Integer> trie = new TrieImpl<>();
        HashSet<Integer> result = new HashSet<>();
        assertEquals(result, trie.deleteAll("YouTube"));
        trie.put("Thou", 17);
        trie.put("You", 18);
        assertEquals(result, trie.deleteAll("YouTube"));
        trie.put("YouTuber", 19);
        assertEquals(result, trie.deleteAll("YouTube"));
    }

    // Test that makes sure case-insensitive and ignores symbols
    // Test that makes sure case-insensitive and ignores symbols
    @Test
    public void deleteAllIsClean() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You!@#$%^&*()_+`~_+[]{}\\|;:'\"<>,.?/Tube", 17);
        trie.put("You!Tube!!", 18);
        HashSet<Integer> result = new HashSet<>();
        result.add(17);
        result.add(18);
        assertEquals(result, trie.deleteAll("youtube"));
        assertEquals(new ArrayList<>(), trie.getAllSorted("YouTube", Comparator.naturalOrder()));
    }

    // Test that makes sure empty when deletes stuff from symbol words
    @Test
    public void deleteAllIgnoresSymbolWords() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You", 17);
        trie.put("YouTube", 18);
        trie.put("YouTube", 19);
        trie.put("YouViewer", 20);
        trie.put("!@#$R%", 21);
        assertEquals(new HashSet<>(), trie.deleteAll("!"));
    }

    // Tests for delete:
    // Makes sure deletes the value
    @Test
    public void deleteDeletes() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("YouTube", 17);
        trie.delete("YouTube", 17);
        ArrayList<Integer> result = new ArrayList<>();
        assertEquals(result, trie.getAllSorted("YouTube", Comparator.naturalOrder()));
    }

    // Makes sure does not delete other values or suffix values
    @Test
    public void deleteOnlyDeletesGivenValue() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("YouTube", 17);
        trie.put("YouTube", 18);
        trie.put("YouTuber", 19);
        trie.put("You", 20);
        trie.delete("YouTube", 17);
        ArrayList<Integer> result = new ArrayList<>();
        result.add(18);
        assertEquals(result, trie.getAllSorted("YouTube", Comparator.naturalOrder()));
        result = new ArrayList<>();
        result.add(19);
        assertEquals(result, trie.getAllSorted("YouTuber", Comparator.naturalOrder()));
        result = new ArrayList<>();
        result.add(20);
        assertEquals(result, trie.getAllSorted("You", Comparator.naturalOrder()));
    }

    // Makes sure returns deleted value
    @Test
    public void deleteReturns() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You", 17);
        trie.put("YouTube", 18);
        trie.put("YouTube", 19);
        trie.put("YouViewer", 20);
        Integer result = 18;
        assertEquals(result, trie.delete("YouTube", 18));
    }

    // Makes sure returns null if nothing deleted, only suffixes, or not in tree
    @Test
    public void deleteReturnsNull() {
        Trie<Integer> trie = new TrieImpl<>();
        assertNull(trie.delete("YouTube", 18));
        trie.put("You", 18);
        trie.put("Thou", 18);
        assertNull(trie.delete("YouTube", 18));
        trie.put("YouTuber", 18);
        assertNull(trie.delete("YouTube", 18));
        trie.put("YouTube", 19);
        assertNull(trie.delete("YouTube", 18));
    }

    // Test that makes sure case-insensitive and ignores symbols
    @Test
    public void deleteIsClean() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You!@#$%^&*()_+`~_+[]{}\\|;:'\"<>,.?/Tube", 17);
        trie.put("You!Tube!!", 18);
        assertEquals(17, trie.delete("youtube", 17));
        assertEquals(18, trie.delete("youtube", 18));
        assertEquals(new ArrayList<>(), trie.getAllSorted("YouTube", Comparator.naturalOrder()));
    }

    // Test that makes sure empty when deletes stuff from symbol words
    @Test
    public void deleteIgnoresSymbolWords() {
        Trie<Integer> trie = new TrieImpl<>();
        trie.put("You", 17);
        trie.put("YouTube", 18);
        trie.put("YouTube", 19);
        trie.put("YouViewer", 20);
        trie.put("!@#$R%", 21);
        assertNull(trie.delete("!", 21));
    }

}
// don't forget to add tests for the delete methods to make sure they ignore words with no letters