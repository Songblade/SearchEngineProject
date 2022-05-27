package edu.yu.cs.com1320.project.stage5;

import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentStoreImpl;
import edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentStoreTest {
    // now I will do my tests on memory storage
    // Important: to make sure things work, since I already have faith in the files
        // make sure to delete all documents at the end of each test
    private final DocumentStore store;
    private final File directory;
    private final Document[] docs;
    private final File[] paths;
    private final InputStream[] streams;
    private final URI[] uris;

    public DocumentStoreTest() throws URISyntaxException, IOException {
        // note that I will have 8 documents, but only 6 in the store at first, so I can add more later
        // of the last 2, one will be byte of normal size, and the second is a longer word
        // set up bytes
        directory = new File("C:/Users/shimm/coding/junk/stage5Memory");

        paths = new File[8];
        paths[0] = new File(directory, "java.sun.com/index.html.json");
        paths[1] = new File(directory, "java.sun.com/outdex.html.json");
        paths[2] = new File(directory, "java.sun.com/insideoutdex.html.json");
        paths[3] = new File(directory, "java.sun.com/outsideindex.html.json");
        paths[4] = new File(directory, "java.sun.com/rightsideleftdex.html.json");
        paths[5] = new File(directory, "java.sun.com/pokedex.html.json");
        paths[6] = new File(directory, "thisIsDoc7.longDoc.com.json");
        paths[7] = new File(directory, "thisIsDoc8.bytes.com.json");

        for (File file : paths) { // won't delete folders, but will stop tests from breaking
            if (file.exists()) {
                file.delete();
            }
        }

        byte[][] bytes = new byte[8][10];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 10; j++) {
                bytes[i][j] = (byte) (i * j);
            }
        }
        // set up strings
        String[] words = {"ru eating?", "unused", "I eat pie", "unused", "You eat!!!", "unused", "biggerThanNormal"};
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
        Document[] docsInit = {new DocumentImpl(uris[0], words[0], null), new DocumentImpl(uris[1], bytes[1]), new DocumentImpl(uris[2], words[2], null),
                new DocumentImpl(uris[3], bytes[3]), new DocumentImpl(uris[4], words[4], null), new DocumentImpl(uris[5], bytes[5]),
                new DocumentImpl(uris[6], words[6], null), new DocumentImpl(uris[7], bytes[7])};
        docs = docsInit;
        // set up store itself
        store = new DocumentStoreImpl(directory);
        store.putDocument(streams[0], uris[0], DocumentFormat.TXT);
        store.putDocument(streams[1], uris[1], DocumentFormat.BINARY);
        store.putDocument(streams[2], uris[2], DocumentFormat.TXT);
        store.putDocument(streams[3], uris[3], DocumentFormat.BINARY);
        store.putDocument(streams[4], uris[4], DocumentFormat.TXT);
        store.putDocument(streams[5], uris[5], DocumentFormat.BINARY);
    }

    // tests that when I have no limit, I can make a bunch of files, and none of them go to disk
    @Test
    public void noLimitNoDisk() {
        for (File file : paths) {
            assertFalse(file.exists());
        }
    }

    // tests that when I go above count, the first-made file goes to disk
    @Test
    public void testCountLimitWorks() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        paths[0].deleteOnExit();
    }

    // tests that when I go above byte, the first-made file goes to disk
    @Test
    public void testByteLimitWorks() throws IOException {
        store.setMaxDocumentBytes(65);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        paths[0].deleteOnExit();
    }

    // tests that can both be used together, when byte limit triggers first
    @Test
    public void testBothLimitsWorkTogetherByte() throws IOException {
        store.setMaxDocumentBytes(65);
        store.setMaxDocumentCount(7);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        paths[0].deleteOnExit();
    }

    // and when count limit triggers first
    @Test
    public void testBothLimitsWorkTogetherCount() throws IOException {
        store.setMaxDocumentBytes(100);
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        paths[0].deleteOnExit();
    }

    // tests that can change count limit later
    @Test
    public void testCountLimitChangeLater() throws IOException {
        store.setMaxDocumentCount(6);
        store.setMaxDocumentCount(7);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertFalse(paths[0].exists());
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        paths[0].deleteOnExit();
    }

    // and byte limit
    @Test
    public void testByteLimitChangeLater() throws IOException {
        store.setMaxDocumentBytes(65);
        store.setMaxDocumentBytes(75);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertFalse(paths[0].exists());
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        paths[0].deleteOnExit();
    }

    // tests put replacement does not overload count
    @Test
    public void putReplacementDoesNotCauseOverloadCount() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[7], uris[0], DocumentFormat.BINARY);
        assertFalse(paths[0].exists());
        assertFalse(paths[1].exists());
    }

    // tests put replacement does not overload byte
    @Test
    public void putReplacementDoesNotCauseOverloadBytes() throws IOException {
        store.setMaxDocumentBytes(61);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[7], uris[0], DocumentFormat.BINARY);
        assertFalse(paths[0].exists());
        assertFalse(paths[1].exists());
    }

    // tests put replacement does overload byte if above limit now
    @Test
    public void putReplacementCausesOverloadIfAboveByteLimit() throws IOException {
        store.setMaxDocumentBytes(61);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[0], DocumentFormat.TXT);
        assertFalse(paths[0].exists());
        assertTrue(paths[1].exists());
        assertFalse(paths[2].exists());

        paths[1].deleteOnExit();
    }

    // tests byte limit can cause overload
    @Test
    public void byteMemoryCrunch() {
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.setMaxDocumentBytes(41);
        assertTrue(paths[0].exists());
        assertTrue(paths[1].exists());
        assertFalse(paths[2].exists());

        paths[0].deleteOnExit();
        paths[1].deleteOnExit();
    }

    // tests count limit can cause overload
    @Test
    public void countMemoryCrunch() {
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.setMaxDocumentCount(4);
        assertTrue(paths[0].exists());
        assertTrue(paths[1].exists());
        assertFalse(paths[2].exists());

        paths[0].deleteOnExit();
        paths[1].deleteOnExit();
    }

    // tests delete can free up space
    @Test
    public void deleteFreesUpMemory() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.deleteDocument(uris[0]);
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertFalse(paths[1].exists());
        assertFalse(paths[2].exists());
    }

    // tests undoing a put can free up space
    @Test
    public void undoingPutFreesUpMemory() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.undo();
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertFalse(paths[0].exists());
        assertFalse(paths[1].exists());
    }

    // tests an overload can put 2 documents on the disk
    @Test
    public void overloadCanRemove2() throws IOException {
        store.setMaxDocumentBytes(61);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertTrue(paths[0].exists());
        assertTrue(paths[1].exists());
        assertFalse(paths[2].exists());

        paths[0].deleteOnExit();
        paths[1].deleteOnExit();
    }

    // tests getDocument counts as a use
    @Test
    public void getDocumentCountsAsUse() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.getDocument(uris[0]);
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertFalse(paths[0].exists());
        assertTrue(paths[1].exists());
        assertFalse(paths[2].exists());

        paths[1].deleteOnExit();
    }

    // tests putDocument counts as a use
    @Test
    public void putDocumentCountsAsUse() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[7], uris[0], DocumentFormat.BINARY);
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertFalse(paths[0].exists());
        assertTrue(paths[1].exists());
        assertFalse(paths[2].exists());

        paths[1].deleteOnExit();
    }

    // tests search counts as a use
    @Test
    public void searchCountsAsUse() throws IOException {
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.setMaxDocumentCount(4);
        // should get rid of 0 and 1, since they are irrelevant here
        store.search("eat");
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertFalse(paths[2].exists());
        assertTrue(paths[3].exists());
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertFalse(paths[2].exists()); // this could cause errors, because the old version used getDocument
        assertTrue(paths[3].exists());
        assertFalse(paths[4].exists());
        assertTrue(paths[5].exists());

        paths[0].deleteOnExit();
        paths[1].deleteOnExit();
        paths[3].deleteOnExit();
        paths[5].deleteOnExit();
    }

    // tests searchByPrefix counts as a use
    @Test
    public void searchPrefixCountsAsUse() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.searchByPrefix("eat");
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertFalse(paths[0].exists());
        assertTrue(paths[1].exists());
        store.putDocument(streams[7], uris[7], DocumentFormat.BINARY);
        assertFalse(paths[0].exists()); // this could cause errors, because the old version used getDocument
        assertTrue(paths[1].exists());
        assertFalse(paths[2].exists());
        assertTrue(paths[3].exists());

        paths[1].deleteOnExit();
        paths[3].deleteOnExit();
    }

    // tests can get a document from disk, and that is no longer in the disk, when nothing else would go on
    @Test
    public void testGetRemovesDocFromDisk() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        store.setMaxDocumentCount(7);
        assertEquals(docs[0], store.getDocument(uris[0]));
        assertFalse(paths[0].exists());
        assertFalse(paths[1].exists());
    }

    // same as above, but a new document is put to disk
    @Test
    public void testGetRemovesDocFromDiskAndReplaces() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        store.getDocument(uris[0]);
        assertFalse(paths[0].exists());
        assertTrue(paths[1].exists());

        paths[1].deleteOnExit();
    }

    // tests can put a document replacing something from disk, nothing is on disk now
    @Test
    public void testPutRemovesDocFromDisk() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        store.setMaxDocumentCount(7);
        store.putDocument(streams[7], uris[0], DocumentFormat.BINARY);
        assertFalse(paths[0].exists());
        assertFalse(paths[1].exists());
    }

    // same as above, but now something else is on the disk
    @Test
    public void testPutRemovesDocFromDiskAndReplaces() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        store.putDocument(streams[7], uris[0], DocumentFormat.BINARY);
        assertFalse(paths[0].exists());
        assertTrue(paths[1].exists());

        paths[1].deleteOnExit();
    }

    // tests can delete a document on the disk, no longer there
    @Test
    public void testDeleteRemovesDocFromDisk() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        store.deleteDocument(uris[0]);
        assertFalse(paths[0].exists());
        assertFalse(paths[1].exists());
        assertNull(store.getDocument(uris[0])); // and can no longer get it
    }

    // tests also works for deleteAll
    @Test
    public void testDeleteAllRemovesDocFromDisk() {
        store.setMaxDocumentCount(6);
        // I want to interact with Documents 0, 1, 3, and 5, so that 2 and 4 get destroyed by the memory crunch
        store.getDocument(uris[0]);
        store.getDocument(uris[1]);
        store.getDocument(uris[3]);
        store.getDocument(uris[5]);

        assertFalse(paths[2].exists());
        assertFalse(paths[4].exists());

        store.setMaxDocumentCount(4);
        assertTrue(paths[2].exists());
        assertTrue(paths[4].exists());

        store.deleteAll("eat");
        assertFalse(paths[2].exists());
        assertFalse(paths[4].exists());
        assertNull(store.getDocument(uris[2])); // and can no longer get them
        assertNull(store.getDocument(uris[4]));
    }

    // and deleteAllByPrefix
    @Test
    public void testDeleteAllWithPrefixRemovesDocFromDisk() {
        store.setMaxDocumentCount(6);
        // I want to interact with Documents 1, 3, 4, and 5, so that 0 and 2 get destroyed by the memory crunch
        store.getDocument(uris[1]);
        store.getDocument(uris[3]);
        store.getDocument(uris[4]);
        store.getDocument(uris[5]);

        assertFalse(paths[0].exists());
        assertFalse(paths[2].exists());

        store.setMaxDocumentCount(4);
        assertTrue(paths[0].exists());
        assertTrue(paths[2].exists());

        store.deleteAllWithPrefix("eat");
        assertFalse(paths[0].exists());
        assertFalse(paths[2].exists());
        assertNull(store.getDocument(uris[0])); // and can no longer get them
        assertNull(store.getDocument(uris[2]));
    }

    // tests deleteAll can delete when part is on disk and part in memory
    @Test
    public void testDeleteAllWorksDiskAndMemory() {
        store.setMaxDocumentCount(6);
        // I want to interact with Documents 0 and 1, so that 2 gets destroyed by the memory crunch
        store.getDocument(uris[0]);
        store.getDocument(uris[1]);

        store.setMaxDocumentCount(5);

        store.deleteAll("eat");
        assertNull(store.getDocument(uris[2]));
        assertNull(store.getDocument(uris[4]));
    }

    // I will also need to make something for how undo interacts with memory, but first need to figure out
        // how exactly undo interacts with memory
    // test that if the document was on the disk, is deleted, and is undone, it is on the disk again
    @Test
    public void testDeleteUndoWhenWasOnDiskPutsBackOnDisk() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        store.deleteDocument(uris[0]);
        assertFalse(paths[0].exists());
        assertFalse(paths[1].exists());

        store.undo();
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());
    }

    // same as before but with put replace
    @Test
    public void testPutReplaceWhenWasOnDiskPutsBackOnDisk() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        store.setMaxDocumentCount(10);

        store.putDocument(streams[7], uris[0], DocumentFormat.BINARY);
        assertFalse(paths[0].exists());
        assertFalse(paths[1].exists());

        store.undo();
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());
    }

    // same as previous but with put(null)
    @Test
    public void testPutNullWhenWasOnDiskPutsBackOnDisk() throws IOException {
        store.setMaxDocumentCount(6);
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        store.putDocument(streams[6], uris[6], DocumentFormat.TXT);
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());

        store.putDocument(null, uris[0], DocumentFormat.BINARY);
        assertFalse(paths[0].exists());
        assertFalse(paths[1].exists());

        store.undo();
        assertTrue(paths[0].exists());
        assertFalse(paths[1].exists());
    }

    // make sure also works with deleteAll
    @Test
    public void testDeleteAllWhenOnDiskPutsBackOnDisk() {
        // I want to interact with Documents 0 and 1, so that 2 gets destroyed by the memory crunch
        store.getDocument(uris[0]);
        store.getDocument(uris[1]);

        store.setMaxDocumentCount(5);
        assertTrue(paths[2].exists());
        assertFalse(paths[4].exists());

        store.deleteAll("eat");

        assertFalse(paths[2].exists());
        assertFalse(paths[4].exists());

        store.undo();

        assertTrue(paths[2].exists());
        assertFalse(paths[4].exists());
    }

    // now testing deleteAllWithPrefix
    @Test
    public void testDeleteAllWithPrefixUndoWhenOnDiskPutsBackOnDisk() {
        store.setMaxDocumentCount(6);
        // I want to interact with Documents 1, 3, 4, and 5, so that 0 and 2 get destroyed by the memory crunch
        store.getDocument(uris[1]);
        store.getDocument(uris[3]);
        store.getDocument(uris[4]);
        store.getDocument(uris[5]);

        assertFalse(paths[0].exists());
        assertFalse(paths[2].exists());

        store.setMaxDocumentCount(4);
        assertTrue(paths[0].exists());
        assertTrue(paths[2].exists());

        store.deleteAllWithPrefix("eat");
        assertFalse(paths[0].exists());
        assertFalse(paths[2].exists());

        store.undo();
        assertTrue(paths[0].exists());
        assertTrue(paths[2].exists());
    }

    // tests that if it was not on the disk, undo does not make it there
    @Test
    public void testDeleteUndoWhenWasNotOnDiskDoesNotPutBackOn() {
        for (int i = 0; i < 6; i++) {
            assertEquals(docs[i], store.getDocument(uris[i]));
        }
        assertFalse(paths[0].exists());
        assertFalse(paths[1].exists());

        store.deleteDocument(uris[0]);
        assertFalse(paths[0].exists());
        assertFalse(paths[1].exists());

        store.undo();
        assertFalse(paths[0].exists());
        assertFalse(paths[1].exists());
    }

    // tests for the constructors
    // tests that if I use the default constructor, putDocument makes a file in user.dir
    @Test
    public void defaultConstructorWorks() throws URISyntaxException, IOException {
        DocumentStore defStore = new DocumentStoreImpl();
        File file = new File(System.getProperty("user.dir"), "place.json");
        assertFalse(file.exists());

        InputStream stream = new ByteArrayInputStream("A whole bunch of stuff that is more than 1 byte".getBytes());
        defStore.putDocument(stream, new URI("http://place"), DocumentFormat.TXT);
        defStore.setMaxDocumentBytes(1);

        assertTrue(file.exists());

        file.deleteOnExit();
    }

    // tests that if I give a folder, putDocument makes a file there
    @Test
    public void fileConstructorWorks() {
        File file = new File(directory, "java.sun.com/index.html.json");
        assertFalse(file.exists());

        store.setMaxDocumentCount(5);

        assertTrue(file.exists());

        file.deleteOnExit();
    }

}

/*
I need to make sure that I am not supposed to remove anything from the stack except when undoing it
I need to go through and find all the printStackTraces and find workarounds to throw exceptions anyway
 */