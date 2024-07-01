package edu.yu.cs.com1320.project;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class tests that the project works on Linux, using a subset of my Windows tests
 */
public class LinuxTest {

    private final DocumentPersistenceManager manager;

    public LinuxTest() {
        File directory = new File("~/linux_test");
        manager = new DocumentPersistenceManager(directory);
        //deleteAllFiles(directory);
    }

    @Test
    public void serializeCreatesFileTxt() throws URISyntaxException, IOException {
        File file = new File("~/linux_test/insideoutdex/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://insideoutdex/ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text WHo Cares", null);
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
    }

    @Test
    public void serializeCreatesFileBinary() throws URISyntaxException, IOException {
        File file = new File("~/linux_test/outdex/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://outdex/ouch");
        byte[] bytes = {(byte) 0, (byte) 1, (byte) 2, (byte) 3};
        Document newDoc = new DocumentImpl(docURI, bytes);
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
    }

    // test that when I deserialize, the word map, URI, and text are the same as before
    @Test
    public void deserializeReadsTxtFile() throws URISyntaxException, IOException {
        File file = new File("~/linux_test/outdex/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://outdex/ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text Who Cares", null);
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
        Document deserialized = manager.deserialize(docURI);

        assertEquals(docURI, deserialized.getKey());
        assertEquals("Random Text Who Cares", deserialized.getDocumentTxt());
        assertNull(deserialized.getDocumentBinaryData());
        assertEquals(0, deserialized.getLastUseTime());

        HashMap<String, Integer> wordMap = new HashMap<>();
        wordMap.put("random", 1);
        wordMap.put("text", 1);
        wordMap.put("who", 1);
        wordMap.put("cares", 1);
        assertEquals(wordMap, deserialized.getWordMap());
    }

    @Test
    public void deserializeReadsLongBinaryFile() throws URISyntaxException, IOException {
        File file = new File("~/linux_test/outdex/longOuch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://outdex/longOuch");
        byte[] bytes = new byte[100000];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (i % 256);
        }
        Document newDoc = new DocumentImpl(docURI, bytes);
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
        Document deserialized = manager.deserialize(docURI);

        assertEquals(docURI, deserialized.getKey());
        assertArrayEquals(bytes, deserialized.getDocumentBinaryData(), "Should have " + Arrays.toString(bytes)
                + ", have " + Arrays.toString(deserialized.getDocumentBinaryData()));
        assertNull(deserialized.getDocumentTxt());
        assertEquals(new HashMap<>(), deserialized.getWordMap());
        assertEquals(0, deserialized.getLastUseTime());
    }

    @Test
    public void deleteWorks() throws URISyntaxException, IOException {
        File file = new File("~/linux_test/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text Who Cares", null);
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
        manager.deserialize(docURI); // can deserialize now
        manager.delete(docURI);
        // show how deleted
        assertFalse(file.exists());
    }

    // tests that delete also deletes empty folders
    @Test
    public void deleteDeletesFolders() throws URISyntaxException, IOException {
        File file = new File("~/linux_test/folderdex/otherwiseEmpty/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://folderdex/otherwiseEmpty/ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text Who Cares", null);
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
        File folder = new File("~/linux_test/folderdex");
        assertTrue(folder.exists());

        manager.delete(docURI);
        // show how deleted
        assertFalse(file.exists());

        assertFalse(folder.exists());
    }

    @Test
    public void managerWorksNullDirectory() throws URISyntaxException, IOException {
        DocumentPersistenceManager defaultManager = new DocumentPersistenceManager(null);

        File file = new File(System.getProperty("user.dir"), "outdex/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://outdex/ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text Who Cares", null);
        defaultManager.serialize(docURI, newDoc);
        assertTrue(file.exists());
        Document deserialized = defaultManager.deserialize(docURI);

        assertEquals(docURI, deserialized.getKey());
        assertEquals("Random Text Who Cares", deserialized.getDocumentTxt());
        assertNull(deserialized.getDocumentBinaryData());
        assertEquals(0, deserialized.getLastUseTime());

        HashMap<String, Integer> wordMap = new HashMap<>();
        wordMap.put("random", 1);
        wordMap.put("text", 1);
        wordMap.put("who", 1);
        wordMap.put("cares", 1);
        assertEquals(wordMap, deserialized.getWordMap());
    }
}
