package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import jakarta.xml.bind.DatatypeConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {

    // used to serialize and deserialize stuff
    private static class JsonSerialManager implements JsonSerializer<Document>, JsonDeserializer<Document> {

        /**
         * @param document that is being serialized
         * @param type null, I am ignoring it
         * @param context null, I am ignoring it
         * @return a JsonElement representation, for you to figure out what to do with
         */
        @Override
        public JsonElement serialize(Document document, Type type, JsonSerializationContext context) {
            // I am going to completely ignore context and say it should be null, because I can't find
                // any information on how to make one or use one
            // I will also ignore type, since I know for a fact this is Document type
            // so, first I create a JsonObject to return
            JsonObject object = new JsonObject();
            // then, I add the uri, which I will do as a String, since that makes it easier
            object.addProperty("uri", document.getKey().toString());
            // then, I add the string, or null
            // then, I add the byte array, or null
            // we were given something to help parse it, but since I don't understand it,
                // I can't use it, and I don't see why it is necessary
            // This way, if it has text as a property, it is txt, otherwise it is binary
            if (document.getDocumentBinaryData() == null) {
                object.addProperty("text", document.getDocumentTxt());
                // then, I add the map
                // we don't need this in bytes because no words
                Gson gson = new Gson();
                object.addProperty("wordCount", gson.toJson(document.getWordMap(), new TypeToken<Map<String, Integer>>(){}.getType()));
            } else {
                String encodedBytes = DatatypeConverter.printBase64Binary(document.getDocumentBinaryData());
                object.addProperty("binaryData", encodedBytes);
            }
            //*/

            return object;
        }

        /**
         * @param element that is being decoded
         * @param type will not be used, this is a document
         * @param context will not be used, I don't even know how
         * @return a decoded document
         */
        @Override
        public Document deserialize(JsonElement element, Type type, JsonDeserializationContext context)  {
            if (!element.isJsonObject()) {
                throw new JsonParseException("element being deserialized is not an object");
            }
            JsonObject object = (JsonObject) element;

            Document doc;

            URI uri = null;
            try {
                uri = new URI(object.get("uri").getAsString());
            } catch (URISyntaxException e) {
                e.printStackTrace(); // I hope this doesn't happen
            }
            if (uri == null) {
                throw new JsonParseException("Really a problem with your URI, but Java is being annoying");
            }
            Gson gson = new Gson(); // will need it to decode
            if (object.has("text")) { // get the right type of doc
                String text = object.get("text").getAsString();
                // the following lines should extract the wordCount
                // if this works, I am extracting the map from the string, and then getting the map from that
                String jsonMap = gson.fromJson(object.get("wordCount"), String.class);
                Map<String, Integer> wordMap = gson.fromJson(jsonMap, new TypeToken<Map<String, Integer>>(){}.getType());
                doc = new DocumentImpl(uri, text, wordMap);
                // we don't need this if bytes, because no words
            } else {
                byte[] bytes = DatatypeConverter.parseBase64Binary(object.get("binaryData").toString());
                doc = new DocumentImpl(uri, bytes);
            }

            return doc;
        }
    }

    private final File baseDir; // the location where all the files are

    public DocumentPersistenceManager(File baseDir){
        this.baseDir = Objects.requireNonNullElseGet(baseDir, () -> new File(System.getProperty("user.dir")));
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        File file = turnURIToFile(uri);
        createAllNeededFiles(file);
        FileWriter writer = new FileWriter(file);
        // I should now be able to write the GSON stuff to the json file// Now I will create the gson to do it
        Gson gson = setUpGson();
        // that should have made a gson that does documents right
        writer.write(gson.toJson(val, Document.class)); // should write the document to the file
        writer.close();
    }

    /**
     * Turns the URI we are given into a File in the right directory
     * Also makes sure we can read and write here, if applicable
     * @param uri given
     * @return a file in the right directory
     */
    private File turnURIToFile(URI uri) {
        String fileEndName = uri.getSchemeSpecificPart(); // should get rid of https:/
        fileEndName += ".json"; // because we are making a json file
        File file = new File(baseDir, fileEndName);
        if (file.exists() && (!file.canRead() || !file.canWrite())) {
            throw new IllegalArgumentException("Cannot read or write in this location");
        }
        return file;
    }

    /**
     * Creates all files and directories needed in order to store stuff there
     */
    private void createAllNeededFiles(File fullFile) throws IOException {
        String fileName = fullFile.getPath();
        int lastSlash = fileName.lastIndexOf(File.separatorChar);
        if (lastSlash > 0) {
            String directoryName = fileName.substring(0, fileName.lastIndexOf(File.separatorChar));
            if (!directoryName.isEmpty()) {
                File directory = new File(directoryName);
                Files.createDirectories(directory.toPath()); // doesn't throw errors if some directories exist
            }
        }
        File file = new File(fileName);
        if (!file.exists()) {
            Files.createFile(file.toPath());
        }
    }

    private Gson setUpGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Document.class, new JsonSerialManager())
                .create();
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        // First, I need to actually get the data
        File file = turnURIToFile(uri);
        if (!file.exists()) {
            return null; // that is what we were told to do
        }
        Scanner fileScanner = new Scanner(file);
        // it looks like my files only take up one line, so that is what I will take
        if (!fileScanner.hasNext()) {
            throw new IllegalStateException("File is empty");
        }
        String fileContents = fileScanner.nextLine();
        fileScanner.close(); // should stop some problems
        // then I need to decode it and build the file, and return it
        Gson gson = setUpGson();
        return gson.fromJson(fileContents, Document.class);
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        File file = turnURIToFile(uri);
        if (!file.exists()) {
            return false; // because we can't delete it
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("This is a directory, those aren't stored for you");
        }
        boolean didDelete = file.delete(); // should return false if doesn't exist
        deleteFolders(uri.getSchemeSpecificPart());
        return didDelete;
    }

    /**
     * Recursively deletes all folders that aren't empty
     * @param fileName the scheme-specific part of the file
     */
    private void deleteFolders(String fileName) {
        if (fileName.indexOf('/') == -1) { // if this is the document, or we finished the last folder
            return;
        }
        String nextFolderName = fileName.substring(0, fileName.lastIndexOf("/"));
        File nextFolder = new File(baseDir, nextFolderName);
        if (!nextFolder.exists()) { // for those times when the file name is "/"
            return;
        }
        if (!nextFolder.isDirectory()) {
            throw new IllegalStateException("You have a file in a file. Something is wrong");
        }
        if (nextFolder.listFiles().length == 0) {
            nextFolder.delete();
            deleteFolders(nextFolderName);
        }
    }
}
