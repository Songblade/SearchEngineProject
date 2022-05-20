package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
//import jakarta.xml.bind.DatatypeConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Map;
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
            /* All this was code that I planned on using
                But for some reason Json just ignores my code and serializes it by looking at every field
                not marked transient itself and saving the document accordingly
                Since it is saving it in a nice way, I will just listen to it and ignore this
            object.addProperty("uri", document.getKey().toString());
            // then, I add the string, or null
            // then, I add the byte array, or null
            // we were given something to help parse it, but since I don't understand it,
                // I can't use it, and I don't see why it is necessary
            // This way, if it has text as a property, it is txt, otherwise it is binary
            if (document.getDocumentBinaryData() == null) {
                object.addProperty("text", document.getDocumentTxt());
                // then, I add the map, which I think I will turn into 2 arrays and encode each
                // we don't need this in bytes because no words
                JsonArray wordList = new JsonArray();
                JsonArray wordNumbers = new JsonArray();
                Map<String, Integer> wordCount = document.getWordMap();
                for (String word : wordCount.keySet()) {
                    wordList.add(word);
                    wordNumbers.add(wordCount.get(word));
                }
                object.add("wordCount-words", wordList);
                object.add("wordCount-numbers", wordNumbers);
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
            Gson gson = new DocumentPersistenceManager(null).setUpGson(); // will need it to decode
            if (object.has("text")) { // get the right type of doc
                String text = object.get("text").getAsString();
                doc = new DocumentImpl(uri, text);
                // the following line should extract the wordCount
                Map<String, Integer> wordCount = gson.fromJson(object.get("wordCount"), new TypeToken<Map<String, Integer>>(){}.getType());
                doc.setWordMap(wordCount);
                // now we need to reconstruct a new Map for no good reason
                // getting each of the stored arrays
                // this is my own way of doing things, back when I thought that it would actually
                    // serialize it the way I wanted
                /*JsonArray words = object.getAsJsonArray("wordCount-words");
                JsonArray numbers = object.getAsJsonArray("wordCount-numbers");
                Map<String, Integer> wordMap = new HashMap<>();
                for (int i = 0; i < words.size(); i++) { // unpacking everything
                    wordMap.put(words.get(i).getAsString(), numbers.get(i).getAsInt());
                }
                doc.setWordMap(wordMap);
                // we don't need this if bytes, because no words
                 */
            } else {
                //byte[] bytes = DatatypeConverter.parseBase64Binary(object.get("binaryData").toString());
                byte[] bytes = gson.fromJson(object.get("binaryData"), byte[].class);
                doc = new DocumentImpl(uri, bytes);
            }

            return doc;
        }

    }

    private File baseDir; // the location where all the files are

    public DocumentPersistenceManager(File baseDir){
        this.baseDir = baseDir;
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
        File file = turnURIToFile(uri);
        createAllNeededFiles(file);
        FileWriter writer = new FileWriter(file);
        // I should now be able to write the GSON stuff to the json file// Now I will create the gson to do it
        Gson gson = setUpGson();
        // that should have made a gson that does documents right
        writer.write(gson.toJson(val)); // should write the document to the file
        writer.close();
    }

    /**
     * Turns the URI we are given into a File in the right directory
     * Also makes sure we can read and write here, if applicable
     * @param uri given
     * @return a file in the right directory
     */
    private File turnURIToFile(URI uri) {
        String fileEndName = uri.getAuthority() + uri.getPath(); // should get rid of https:\
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
        int lastSlash = fileName.lastIndexOf('\\');
        if (lastSlash > 0) {
            String directoryName = fileName.substring(0, fileName.lastIndexOf('\\'));
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
            throw new IllegalArgumentException("File doesn't exist");
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
        if (file.isDirectory()) {
            throw new IllegalArgumentException("This is a directory, those aren't stored for you");
        }
        return file.delete(); // should return false if doesn't exist
    }
}
