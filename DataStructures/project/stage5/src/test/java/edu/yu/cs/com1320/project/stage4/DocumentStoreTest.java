package edu.yu.cs.com1320.project.stage4;

import edu.yu.cs.com1320.project.stage4.impl.*;
import edu.yu.cs.com1320.project.stage4.DocumentStore.DocumentFormat;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentStoreTest {

    private DocumentStore store;
    private byte[][] bytes;
    private String[] words;
    private InputStream[] streams;
    private URI[] uris;
    private Document[] docs;

    public DocumentStoreTest() throws URISyntaxException, IOException {
        // note that I will have 8 documents, but only 6 in the store at first, so I can add more later
        // of the last 2, one will be byte of normal size, and the second is a longer word
        // set up bytes
        bytes = new byte[8][10];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 10; j++) {
                bytes[i][j] = (byte) (i * j);
            }
        }
        // set up strings
        String[] wordsInit = {"ru eating?", "unused", "I eat pie", "unused", "You eat!!!", "unused", "biggerThanNormal"};
        words = wordsInit;
        // set up Streams
        streams = new InputStream[8];
        for (int i = 0; i < 8; i++) {
            if (i % 2 == 0) {
                streams[i] = new ByteArrayInputStream(words[i].getBytes());
            } else {
                streams[i] = new ByteArrayInputStream(bytes[i]);
            }
        }
        // set up uris
        URI[] urisInit = {new URI("http://java.sun.com/index.html"), new URI("http://java.sun.com/outdex.html"),
                new URI("http://java.sun.com/insideoutdex.html"), new URI("http://java.sun.com/outsideindex.html"),
                new URI("http://java.sun.com/rightsideleftdex.html"), new URI("http://java.sun.com/pokedex.html"),
                new URI("thisIsDoc7.longDoc.com"), new URI("thisIsDoc8.bytes.com")};
        uris = urisInit;
        // sets up documents
        Document[] docsInit = {new DocumentImpl(uris[0], words[0]), new DocumentImpl(uris[1], bytes[1]), new DocumentImpl(uris[2], words[2]),
                new DocumentImpl(uris[3], bytes[3]), new DocumentImpl(uris[4], words[4]), new DocumentImpl(uris[5], bytes[5]),
                new DocumentImpl(uris[6], words[6]), new DocumentImpl(uris[7], bytes[7])};
        docs = docsInit;
        // set up store itself
        store = new DocumentStoreImpl();
        store.putDocument(streams[0], uris[0], DocumentFormat.TXT);
        store.putDocument(streams[1], uris[1], DocumentFormat.BINARY);
        store.putDocument(streams[2], uris[2], DocumentFormat.TXT);
        store.putDocument(streams[3], uris[3], DocumentFormat.BINARY);
        store.putDocument(streams[4], uris[4], DocumentFormat.TXT);
        store.putDocument(streams[5], uris[5], DocumentFormat.BINARY);
    }

    // tests for memory:
    // I want to make sure that doc count limit works
    @Test
    public void testCountLimitWorks() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNull(store.getDocument(uris[0]));
        assertNotNull(store.getDocument(uris[1]));
    }

    // I want to make sure that doc byte limit works
    @Test
    public void testByteLimitWorks() throws IOException {
        store.setMaxDocumentBytes(65);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertNull(store.getDocument(uris[0]));
        assertNotNull(store.getDocument(uris[1]));
    }

    // I want to make sure that they work in tandem, if both are called
    // This one checks byte can trigger even if count doesn't
    @Test
    public void testBothLimitsWorkTogetherByte() throws IOException {
        store.setMaxDocumentBytes(65);
        store.setMaxDocumentCount(7);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNull(store.getDocument(uris[0]));
        assertNotNull(store.getDocument(uris[1]));
    }

    // this one checks that count can trigger even if byte doesn't
    @Test
    public void testBothLimitsWorkTogetherCount() throws IOException {
        store.setMaxDocumentBytes(100);
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNull(store.getDocument(uris[0]));
        assertNotNull(store.getDocument(uris[1]));
    }

    // I want to make sure that for each limit, you can call it and then change it later
    @Test
    public void testCountLimitChangeLater() throws IOException {
        store.setMaxDocumentCount(6);
        store.setMaxDocumentCount(7);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNotNull(store.getDocument(uris[0]));
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertNull(store.getDocument(uris[1]));
        assertNotNull(store.getDocument(uris[2]));
    }

    // this one checks for count
    @Test
    public void testByteLimitChangeLater() throws IOException {
        store.setMaxDocumentBytes(65);
        store.setMaxDocumentBytes(75);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertNotNull(store.getDocument(uris[0]));
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNull(store.getDocument(uris[1]));
        assertNotNull(store.getDocument(uris[2]));
    }

    // put replacement does not cause overload in count
    @Test
    public void putReplacementDoesNotCauseOverloadCount() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[7], uris[0], DocumentFormat.BINARY);
        assertNotNull(store.getDocument(uris[0]));
        assertNotNull(store.getDocument(uris[1]));
    }

    // or in bytes
    @Test
    public void putReplacementDoesNotCauseOverloadBytes() throws IOException {
        store.setMaxDocumentBytes(61);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[7], uris[0], DocumentFormat.BINARY);
        assertNotNull(store.getDocument(uris[0]));
        assertNotNull(store.getDocument(uris[1]));
    }

    // but does if replacement now makes it over the byte limit
    @Test
    public void putReplacementCausesOverloadIfAboveByteLimit() throws IOException {
        store.setMaxDocumentBytes(61);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[0], DocumentFormat.TXT);
        assertNotNull(store.getDocument(uris[0]));
        assertNull(store.getDocument(uris[1]));
        assertNotNull(store.getDocument(uris[2]));
    }

    // make sure that deleting frees up space in memory
    @Test
    public void deleteFreesUpMemory() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.deleteDocument(uris[0]);
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertNotNull(store.getDocument(uris[1]));
        assertNotNull(store.getDocument(uris[2]));
    }

    // make sure that can undo a delete even if memory is full
    @Test
    public void canUndoDeleteWhenMemoryFull() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.deleteDocument(uris[0]);
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        store.undo(uris[0]);
        assertNotNull(store.getDocument(uris[0]));
        assertNull(store.getDocument(uris[1]));
        assertNotNull(store.getDocument(uris[2]));
    }

    // make sure that undoing a put frees up memory
    @Test
    public void undoingPutFreesUpMemory() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.undo();
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertNotNull(store.getDocument(uris[0]));
        assertNotNull(store.getDocument(uris[1]));
        assertNull(store.getDocument(uris[5]));
    }

    // I want to make sure that when memory overload removes the document, it is also removed from the hashtable,
        // trie, and stack
    @Test
    public void testOverloadRemovesFromAll() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNull(store.getDocument(uris[0]));
        assertThrows(IllegalStateException.class, () -> store.undo(uris[0]));
        assertEquals(new ArrayList<>(), store.search("ru"));
    }

    // I want to make sure that if a large addition causes a memory overload, 2 documents can be removed
    @Test
    public void overloadCanRemove2() throws IOException {
        store.setMaxDocumentBytes(61);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNull(store.getDocument(uris[0]));
        assertNull(store.getDocument(uris[1]));
        assertNotNull(store.getDocument(uris[2]));
    }

    // I want to make sure that getDocument counts as a use
    @Test
    public void getDocumentCountsAsUse() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.getDocument(uris[0]);
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNotNull(store.getDocument(uris[0]));
        assertNull(store.getDocument(uris[1]));
        assertNotNull(store.getDocument(uris[2]));
    }

    // putDocument counts as a use
    @Test
    public void putDocumentCountsAsUse() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[7], uris[0], DocumentFormat.BINARY);
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNotNull(store.getDocument(uris[0]));
        assertNull(store.getDocument(uris[1]));
        assertNotNull(store.getDocument(uris[2]));
    }

    // search counts as a use for all documents it finds
    @Test
    public void searchCountsAsUse() throws IOException {
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.setMaxDocumentCount(4);
        // should get rid of 0 and 1, since they are irrelevant here
        store.search("eat");
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNotNull(store.getDocument(uris[2]));
        assertNull(store.getDocument(uris[3]));
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertNotNull(store.getDocument(uris[2]));
        assertNull(store.getDocument(uris[3]));
        assertNotNull(store.getDocument(uris[4]));
        assertNull(store.getDocument(uris[5]));
    }

    // searchByPrefix counts as a use for all documents it finds
    @Test
    public void searchPrefixCountsAsUse() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.searchByPrefix("eat");
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNotNull(store.getDocument(uris[0]));
        assertNull(store.getDocument(uris[1]));
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertNotNull(store.getDocument(uris[0]));
        assertNull(store.getDocument(uris[1]));
        assertNotNull(store.getDocument(uris[2]));
        assertNull(store.getDocument(uris[3]));
    }

    // undo counts as a use for what it undoes
    @Test
    public void undoCountsAsUse() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.deleteDocument(uris[0]);
        store.undo();
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNotNull(store.getDocument(uris[0]));
        assertNull(store.getDocument(uris[1]));
        assertNotNull(store.getDocument(uris[2]));
    }

    // undo for a CommandSet counts as a use for everything in the CommandSet
    @Test
    public void undoSetCountsAsUse() throws IOException {
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.setMaxDocumentCount(4);
        // should get rid of 0 and 1, since they are irrelevant here
        store.deleteAll("eat");
        store.undo();
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNotNull(store.getDocument(uris[2]));
        assertNull(store.getDocument(uris[3]));
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertNotNull(store.getDocument(uris[2]));
        assertNull(store.getDocument(uris[3]));
        assertNotNull(store.getDocument(uris[4]));
        assertNull(store.getDocument(uris[5]));
    }

    // undo(URI) counts as a use
    @Test
    public void undoURICountsAsUse() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.deleteDocument(uris[0]);
        store.getDocument(uris[3]);
        store.undo(uris[0]);
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertNotNull(store.getDocument(uris[0]));
        assertNull(store.getDocument(uris[1]));
        assertNotNull(store.getDocument(uris[2]));
    }

    // can cause memory crunch with count
    @Test
    public void countMemoryCrunch() {
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.setMaxDocumentCount(4);
        assertNull(store.getDocument(uris[0]));
        assertNull(store.getDocument(uris[1]));
        assertNotNull(store.getDocument(uris[2]));
    }

    // can cause memory crunch with bytes
    @Test
    public void byteMemoryCrunch() {
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.setMaxDocumentBytes(41);
        assertNull(store.getDocument(uris[0]));
        assertNull(store.getDocument(uris[1]));
        assertNotNull(store.getDocument(uris[2]));
    }

    // definitely need to add a test to make sure that I can delete a URI from a commandSet
    // as a review, this is when something was deleted through deleteAll, and then added back in,
    // so it is back in the heap
    // I need to make sure that when I get a memory overflow and lose the document, I also lose the undo in
    // the commandSet, but keep the other document there
    @Test
    public void testCommandSetShenanigans() throws IOException {
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.deleteAll("eat");
        store.putDocument(new ByteArrayInputStream(words[2].getBytes()), uris[2], DocumentFormat.TXT);
        store.getDocument(uris[0]);
        store.setMaxDocumentCount(1); // gets rid of everything but uris[0]
        store.setMaxDocumentCount(6);
        assertNull(store.getDocument(uris[2]));
        assertNull(store.getDocument(uris[4]));
        store.undo();
        assertNull(store.getDocument(uris[2]));
        assertNotNull(store.getDocument(uris[4]));
    }



}
