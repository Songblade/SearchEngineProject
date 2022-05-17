package edu.yu.cs.com1320.project.stage5;

import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentTest {
    // this tests the new stuff in DocumentImpl
    // test that getWordMap returns the word map
    @Test
    public void getWordMapReturns() throws URISyntaxException {
        Document doc = new DocumentImpl(new URI("https://stuff/stuff.com"), "I love bananas and I love them now!");

        HashMap<String, Integer> wordMap = new HashMap<>();
        wordMap.put("i", 2);
        wordMap.put("love", 2);
        wordMap.put("bananas", 1);
        wordMap.put("and", 1);
        wordMap.put("them", 1);
        wordMap.put("now", 1);

        assertEquals(wordMap, doc.getWordMap());
    }

    // test that if we set the wordMap later, that is what we get back
    @Test
    public void setWordMapWorks() throws URISyntaxException {
        Document doc = new DocumentImpl(new URI("https://stuff/stuff.com"), "text doesn't matter here");

        HashMap<String, Integer> wordMap = new HashMap<>();
        wordMap.put("i", 2);
        wordMap.put("love", 2);
        wordMap.put("bananas", 1);
        wordMap.put("and", 1);
        wordMap.put("them", 1);
        wordMap.put("now", 1);
        doc.setWordMap(wordMap);

        assertEquals(wordMap, doc.getWordMap());
    }
}
