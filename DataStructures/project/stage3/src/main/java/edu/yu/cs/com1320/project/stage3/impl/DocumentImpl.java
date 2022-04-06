package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.stage3.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {

    private URI uri; // the URI of the document
    private String text; // the text of the document, null if not applicable
    private byte[] binaryData; // the data of the document, null if not applicable
    private Map<String, Integer> wordCount;

    // constructor that uses text
    public DocumentImpl(URI uri, String text) {
        validateURI(uri);
        if (text == null) {
            throw new IllegalArgumentException("text is null");
        } // Piazza says we don't need to worry about a blank URI
        if (text.equals("")) {
            throw new IllegalArgumentException("text is blank");
        }
        this.uri = uri;
        this.text = text;
        generateWordCount();
    }

    // creates the hashmap that contains the wordcount
    private void generateWordCount() {
        wordCount = new HashMap<>();
        Scanner textParser = new Scanner(text);
        while(textParser.hasNext()) {
            String word = cleanWord(textParser.next());
            if (word.length() > 0) { // if the word isn't just symbols
                wordCount.put(word, 1 + wordCount.getOrDefault(word, 0));
                // this should add 1 to the value, or put 1 + 0 if it isn't there yet
            }
        }
    }

    // this method makes sure a word only contains alphanumerics
    private String cleanWord(String word) {
        String newWord = "";
        for (int i = 0; i < word.length(); i++) {
            if (Character.getType(word.charAt(i)) == Character.UPPERCASE_LETTER) {
                newWord += (Character.toLowerCase(word.charAt(i)));
                // so that it will be lowercase
            }
            if (Character.getType(word.charAt(i)) == Character.LOWERCASE_LETTER ||
                    Character.getType(word.charAt(i)) == Character.DECIMAL_DIGIT_NUMBER) {
                newWord += (Character.toLowerCase(word.charAt(i)));
            }
            // if it isn't a letter or a number, it isn't added
        }
        return newWord;
    }

    public DocumentImpl(URI uri, byte[] binaryData) {
        validateURI(uri);
        if (binaryData == null) {
            throw new IllegalArgumentException("binary data is null");
        }
        if (binaryData.length == 0) {
            throw new IllegalArgumentException("binary data is empty");
        }
        this.uri = uri;
        this.binaryData = Arrays.copyOf(binaryData, binaryData.length);
        this.wordCount = new HashMap<>();
    }

    // if the URI being inputted is null or empty, this will throw errors for my constructors
    private void validateURI(URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI is null");
        }
        if (uri.toASCIIString().isBlank()) {
            throw new IllegalArgumentException("URI is blank");
        }
    }

    /**
     * @return content of text document
     */
    @Override
    public String getDocumentTxt() {
        return text; // if not applicable, will already be null
    }

    /**
     * @return content of binary data document
     */
    @Override
    public byte[] getDocumentBinaryData() {
        if (binaryData == null) {
            return null;
        }
        return Arrays.copyOf(binaryData, binaryData.length);
    }

    /**
     * @return URI which uniquely identifies this document
     */
    @Override
    public URI getKey() {
        return uri;
    }

    /**
     * how many times does the given word appear in the document?
     *
     * @param word
     * @return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    @Override
    public int wordCount(String word) {
        return wordCount.getOrDefault(cleanWord(word), 0);
        // this should return the value, if there is one, or 0, if there isn't one
        // since binary documents initialize an empty list, it should already work there
    }

    /**
     * @return all the words that appear in the document
     */
    @Override
    public Set<String> getWords() {
        return wordCount.keySet();
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData); // no error thrown if null, just adds 0
        return result;
    }

    @Override
    public boolean equals (Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        // equality is determined by hashCode, once same class
        return this.hashCode() == other.hashCode();
    }

/*
    //make sure you get rid of this, or you will fail
    // I am leaving this code here, in case I ever want the print again
    @Override
    public String toString() {
        String value = uri.toString() + ": ";
        if (binaryData == null) {
            value += text;
        } else {
            if (binaryData.length < 10) {
                value += Arrays.toString(binaryData);
            } else {
                value += Arrays.toString(Arrays.copyOf(binaryData, 10)) + " and more";
            }
        }
        return value;
    }
*/

}