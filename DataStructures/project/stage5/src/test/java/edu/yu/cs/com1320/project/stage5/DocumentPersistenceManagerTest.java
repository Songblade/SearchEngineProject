package edu.yu.cs.com1320.project.stage5;

import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage5.impl.DocumentPersistenceManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
        if (directory.exists()) { // so we don't get errors when it hasn't been made yet
            for (File file : Objects.requireNonNull(directory.listFiles())) {
                file.delete(); // getting rid of all files
            }
        }
    }

    // tests for serialize
    // test that when I serialize, there is now a file at this URI (use dif URI from others)
    @Test
    public void serializeCreatesFileTxt() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/insideoutdex/ouch.json");
        assertFalse(file.exists());
        URI docURI = new URI("https://insideoutdex/ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text WHo Cares");
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
    }

    // test that also works for a binary document
    @Test
    public void serializeCreatesFileBinary() throws URISyntaxException, IOException {
        File file = new File("C:/Users/shimm/coding/junk/stage5Tests/outdex/ouch.json");
        assertFalse(file.exists());
        URI docURI = new URI("https://outdex/ouch");
        byte[] bytes = {(byte) 0, (byte) 1, (byte) 2, (byte) 3};
        Document newDoc = new DocumentImpl(docURI, bytes);
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
    }

    // Another test that is immediately directory

    // tests for deserialize
    // test that when I deserialize, the word map, URI, and text are the same as before
    //@Test
    public void deserializeReadsFile() throws URISyntaxException, IOException {
        File file = new File("C:\\Users\\shimm\\coding\\junk\\stage5Tests\\outdex\\ouch.json");
        assertFalse(file.exists());
        URI docURI = new URI("https:\\\\outdex\\ouch");
        Document newDoc = new DocumentImpl(docURI, "Random Text Who Cares");
        manager.serialize(docURI, newDoc);
        assertTrue(file.exists());
        Document deserialized = manager.deserialize(docURI);

        assertEquals(docURI, deserialized.getKey());
        assertEquals("Random Text Who Cares", deserialized.getDocumentTxt());

        HashMap<String, Integer> wordMap = new HashMap<>();
        wordMap.put("random", 1);
        wordMap.put("text", 1);
        wordMap.put("who", 1);
        wordMap.put("cares", 1);
        assertEquals(wordMap, deserialized.getWordMap());
    }

    // same as previous, but for a binary document
    // test that even if the document had a time before, getting it deserialization brings it to 0
    // test that can deserialize a second time, that deserialization doesn't destroy the file
    // I need more tests, but can't think of them

    // tests for remove
    // test that when I delete, the file no longer exists
    // tests that when I delete, deserialize no longer works
    // tests that delete returns true if it worked
    // tests that delete returns false if the file never existed

}
