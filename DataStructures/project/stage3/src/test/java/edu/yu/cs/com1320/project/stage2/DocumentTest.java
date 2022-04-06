package edu.yu.cs.com1320.project.stage2;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage3.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

public class DocumentTest {
    // Ignore the constructor
    public DocumentTest() {}

    // I need to make sure that getDocumentTxt returns correctly, including capitals and symbols
    // I must make sure getBinaryData returns null if it is a text document
    // I need to make sure that getKey returns correctly
    @Test
    public void getDocumentTxtReturns() throws URISyntaxException {
        Document doc = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "Tech is cool!");
        assertEquals("Tech is cool!", doc.getDocumentTxt());
        assertNull(doc.getDocumentBinaryData(), "getDocumentBinaryData should be null");
        assertEquals(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), doc.getKey(), "problem in getKey");
    }

    // I must make sure getText returns null if it is a binary document
    // I need to make sure that getBinaryData returns correctly
    @Test
    public void getDocumentBinaryReturns() throws URISyntaxException {
        URI uriExample = new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html");
        byte[] binary = {(byte) 0};
        byte[] binary2 = {(byte) 0};
        Document doc = new DocumentImpl(uriExample, binary);
        assertNull(doc.getDocumentTxt(), "getDocumentTxt is null");
        assertArrayEquals(binary2, doc.getDocumentBinaryData());
    }
    // I will mostly assume that equals works correctly, because I don't know how to test it correctly, but I
    // must make sure to identical Documents are equal
    @Test
    public void getEqualsTrue() throws URISyntaxException {
        URI uriExample = new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html");
        byte[] binary = {(byte) 0};
        byte[] binary2 = {(byte) 0};
        Document doc1 = new DocumentImpl(uriExample, binary);
        Document doc2 = new DocumentImpl(uriExample, binary2);
        assertTrue(doc1.equals(doc2));
        assertTrue(doc2.equals(doc1));
    }

    // and that two different documents aren't equal, even if URI is
    @Test
    public void getEqualsFalse() throws URISyntaxException {
        URI uriExample = new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html");
        Document doc1 = new DocumentImpl(uriExample, "binary");
        Document doc2 = new DocumentImpl(uriExample, "binary2");
        assertFalse(doc1.equals(doc2));
        assertFalse(doc2.equals(doc1));
    }
    // Or text is equal but URI isn't
    @Test
    public void getEqualsFalseURI() throws URISyntaxException {
        Document doc1 = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "binary");
        Document doc2 = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide.html"), "binary");
        assertFalse(doc1.equals(doc2));
        assertFalse(doc2.equals(doc1));
    }

    // I must make sure that a null URI throws an IllegalArgumentException in the text constructor
    @Test
    public void nullURIIAEText() {
        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(null, "binary");
        });
    }
    // I have no idea how to test if a URI is blank, since I can't figure out how to create one, but I have it
    // in my code, so I hope that is good enough
    // I must make sure that a null String throws IAE
    @Test
    public void nullStringIAE() {
        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), (String) null);
        });
    }
    // and empty string
    @Test
    public void emptyStringIAE() {
        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "");
        });
    }
    // Null URI in the binary constructor
    @Test
    public void nullURIIAEBinary() {
        byte[] binary = {(byte) 0};
        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(null, binary);
        });
    }
    // I must make sure null array throws IAE
    @Test
    public void nullArrayIAE() {
        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), (byte[]) null);
        });
    }

    // and empty array
    @Test
    public void emptyArrayIAE() {
        byte[] binary = {};
        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), binary);
        });
    }

    // Tests for wordCount
    // Test that the only word is returned once
    @Test
    public void wordCountReturnsSingle() throws URISyntaxException {
        Document doc = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "tech");
        assertEquals(1, doc.wordCount("tech"));
    }

    // Tests that if a bunch of unique words, returns 1, even if longer word also
    @Test
    public void wordCountReturnsManySingle() throws URISyntaxException {
        Document doc = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "a technician does skilled and qualified tech work");
        assertEquals(1, doc.wordCount("tech"));
    }

    // Tests that if a word appears a bunch of times in a row, returns correctly
    @Test
    public void wordCountReturnsMultiInRow() throws URISyntaxException {
        Document doc = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "I love tech tech tech a lot");
        assertEquals(3, doc.wordCount("tech"));
    }

    // Tests that if a word appears many times spread out, returns correctly
    @Test
    public void wordCountReturnsMultiSpread() throws URISyntaxException {
        Document doc = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "I love tech jobs, tech products, and tech in general");
        assertEquals(3, doc.wordCount("tech"));
    }

    // Tests that if word not in document, returns 0, even if longer word is
    @Test
    public void wordCountReturns0Missing() throws URISyntaxException {
        Document doc = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "I love tech jobs, tech products, and tech in general");
        assertEquals(0, doc.wordCount("job"));
    }

    // Tests that if binary, returns 0
    @Test
    public void wordCountBinaryReturns0() throws URISyntaxException {
        URI uriExample = new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html");
        byte[] binary = {(byte) 0};
        Document doc = new DocumentImpl(uriExample, binary);
        assertEquals(0, doc.wordCount("job"));
    }

    // Tests that ignores non-alphanumerics
    @Test
    public void wordCountIgnoresNonAlphanumerics() throws URISyntaxException {
        Document doc = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "I love awesome non-alphanumerics! They are awesome!! !!!");
        assertEquals(1, doc.wordCount("nonalphanumerics"));
        assertEquals(1, doc.wordCount("non-alphanumerics"));
        assertEquals(1, doc.wordCount("n-!on-al---p-hanu-m---er-ics"));
        assertEquals(2, doc.wordCount("awesome"));
    }

    // Tests for getWords
    // Tests that works correctly
    @Test
    public void getWordsReturns() throws URISyntaxException {
        Document doc = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "i love tech jobs and tech products and tech in general");
        HashSet<String> result = new HashSet<>();
        result.add("i");
        result.add("love");
        result.add("tech");
        result.add("jobs");
        result.add("products");
        result.add("and");
        result.add("in");
        result.add("general");
        assertEquals(result, doc.getWords());
    }

    // Tests that works right when binary (empty set)
    @Test
    public void getWordsReturnsEmptyBinary() throws URISyntaxException {
        URI uriExample = new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html");
        byte[] binary = {(byte) 0};
        Document doc = new DocumentImpl(uriExample, binary);
        HashSet<String> result = new HashSet<>();
        assertEquals(result, doc.getWords());
    }

    // Tests that non-alphanumerics and capitals are ignored
    @Test
    public void getWordsIgnoresNonAlphanumerics() throws URISyntaxException {
        Document doc = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "I #love tech jobs, tech products, and tech in general! !!");
        HashSet<String> result = new HashSet<>();
        result.add("i");
        result.add("love");
        result.add("tech");
        result.add("jobs");
        result.add("products");
        result.add("and");
        result.add("in");
        result.add("general");
        assertEquals(result, doc.getWords());
    }

    // Tests that returns empty when no words (only non-alphanumerics)
    @Test
    public void getWordsIgnoresSymbolWords() throws URISyntaxException {
        Document doc = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "!@#@#$ #$% !!!");
        HashSet<String> result = new HashSet<>();
        assertEquals(result, doc.getWords());
    }

}