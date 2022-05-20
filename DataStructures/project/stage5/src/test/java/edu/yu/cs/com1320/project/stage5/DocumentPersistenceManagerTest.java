package edu.yu.cs.com1320.project.stage5;

import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentPersistenceManagerTest {

    // I'm not really sure how to test the class because I don't know how it should look like, but I will do my best
    // For everything, I will use the custom directory C:\Users\shimm\coding\junk\stage5Tests
    // Though I am not yet sure how to look there properly
    // if it shows up there properly, it should probably also show up everywhere else
    // I will not create the folders, if this works, it should create them on its own
    // Before each test, in the constructor, I will delete all files from previous runs, because that
        // is what Professor Diament will do

    private DocumentPersistenceManager manager;

    public DocumentPersistenceManagerTest() {
        File directory = new File("C:/Users/shimm/coding/junk/stage5Tests");
        manager = new DocumentPersistenceManager(directory);
        //deleteAllFiles(directory);
    }

    /**
     * Should recursively delete files in the directory, so I can start with them deleted
     * @param directory being cleared
     */
    private void deleteAllFiles(File directory) {
        if (directory.exists() && directory.isDirectory()) { // so we don't get errors when it hasn't been made yet
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isFile()) {
                    file.delete(); // getting rid of all files
                } else if (file.isDirectory()){ // if is directory
                    deleteAllFiles(file);
                    file.delete(); // since now we can, because its files are deleted
                }
            }
        }
    }


    // tests for serialize
    // test that when I serialize, there is now a file at this URI (use dif URI from others)

    @Test
    public void serializeCreatesFileTxt() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/insideoutdex/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://insideoutdex/ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text WHo Cares");
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
    }

    // test that also works for a binary document
    @Test
    public void serializeCreatesFileBinary() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/outdex/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://outdex/ouch");
        byte[] bytes = {(byte) 0, (byte) 1, (byte) 2, (byte) 3};
        Document newDoc = new DocumentImpl(docURI, bytes);
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
    }

    // Another test that is immediately directory
    @Test
    public void serializeCreatesFileImmediateDirectory() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/bigOuchie.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://bigOuchie");
        Document newDoc = new DocumentImpl(docURI, "Random Text WHo Cares");
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
    }

    // tests for deserialize
    // test that when I deserialize, the word map, URI, and text are the same as before
    @Test
    public void deserializeReadsTxtFile() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/outdex/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://outdex/ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text Who Cares");
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

    // same as previous, but for a binary document
    @Test
    public void deserializeReadsBinaryFile() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/outdex/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://outdex/ouch");
        byte[] bytes = {(byte) 0, (byte) 1, (byte) 2, (byte) 3};
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

    // test that even if the document had a time before, getting it deserialization brings it to 0
    @Test
    public void deserializeIgnoresTime() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/outdex/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://outdex/ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text Who Cares");
        newDoc.setLastUseTime(50000);
        assertEquals(50000, newDoc.getLastUseTime());
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
        Document deserialized = manager.deserialize(docURI);

        assertEquals(0, deserialized.getLastUseTime());
    }

    // test that can deserialize a second time, that deserialization doesn't destroy the file
    @Test
    public void deserializeWorksTwice() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/outdex/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://outdex/ouch");
        byte[] bytes = {(byte) 0, (byte) 1, (byte) 2, (byte) 3};
        Document newDoc = new DocumentImpl(docURI, bytes);
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
        Document deserialized2 = manager.deserialize(docURI);

        assertEquals(docURI, deserialized2.getKey());
        assertArrayEquals(bytes, deserialized2.getDocumentBinaryData());
        assertNull(deserialized2.getDocumentTxt());
        assertEquals(new HashMap<>(), deserialized2.getWordMap());
        assertEquals(0, deserialized2.getLastUseTime());
    }

    // test that can deserialize when directly in the directory
    @Test
    public void deserializeWorksDirectlyInDirectory() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text Who Cares");
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

    // I need more tests, but can't think of them

    // tests for delete
    // test that when I delete, the file no longer exists
    @Test
    public void deleteWorks() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text Who Cares");
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
        manager.deserialize(docURI); // can deserialize now
        manager.delete(docURI);
        // show how deleted
        assertFalse(file.exists());
    }

    // tests that when I delete, deserialize no longer works
    @Test
    public void deleteStopsDeserialize() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text Who Cares");
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
        manager.deserialize(docURI); // can deserialize now
        manager.delete(docURI);
        // show how deleted
        assertThrows(IllegalArgumentException.class, ()->manager.deserialize(docURI));
    }

    // tests that delete returns true if it worked
    @Test
    public void deleteReturnsTrueIfWorked() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text Who Cares");
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
        manager.deserialize(docURI); // can deserialize now
        assertTrue(manager.delete(docURI));
    }

    // tests that delete returns false if the file never existed
    @Test
    public void deleteReturnsFalseIfNeverExisted() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/JarJarBinks.json");
        assertFalse(file.exists());
        URI docURI = new URI("https://JarJarBinks");
        assertFalse(manager.delete(docURI));
    }

    // tests that delete returns false if the file existed but has been deleted
    @Test
    public void deleteReturnsFalseIfDeleted() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/ouch.json");
        //assertFalse(file.exists());
        URI docURI = new URI("https://ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text Who Cares");
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
        manager.deserialize(docURI); // can deserialize now
        manager.delete(docURI);
        // now deleting again returns false
        assertFalse(manager.delete(docURI));
    }
}
