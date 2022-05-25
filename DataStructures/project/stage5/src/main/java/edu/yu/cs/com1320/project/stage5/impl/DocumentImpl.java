package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {

    private URI uri; // the URI of the document
    private String text; // the text of the document, null if not applicable
    private byte[] binaryData; // the data of the document, null if not applicable
    private Map<String, Integer> wordCount;
    private transient long lastUseTime;

    /**
     * Creates a text document
     * @param uri where the document is stored
     * @param text contained in the document
     * @param wordCount of the document, or null if you want me to generate it for you
     */
    public DocumentImpl(URI uri, String text, Map<String, Integer> wordCount) {
        validateURI(uri);
        if (text == null) {
            throw new IllegalArgumentException("text is null");
        } // Piazza says we don't need to worry about a blank URI
        if (text.equals("")) {
            throw new IllegalArgumentException("text is blank");
        }
        this.uri = uri;
        this.text = text;
        if (wordCount == null) {
            generateWordCount();
        } else {
            this.wordCount = wordCount;
        }
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
        if (word == null) { // this can only happen when the method is called by wordCount()
            throw new IllegalArgumentException("Word is null");
        }
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

    /**
     * Creates a binary document
     * @param uri where the document is stored
     * @param binaryData that the document holds
     */
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

    /**
     * return the last time this document was used, via put/get or via a search result
     * (for stage 4 of project)
     */
    @Override
    public long getLastUseTime() {
        return lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        lastUseTime = timeInNanoseconds;
    }

    /**
     * @return a copy of the word to count map so it can be serialized
     */
    @Override
    public Map<String, Integer> getWordMap() {
        return Collections.unmodifiableMap(wordCount);
    }

    /**
     * This must set the word to count map during deserialization
     *
     * @param wordMap
     */
    @Override
    public void setWordMap(Map<String, Integer> wordMap) {
        wordCount = Collections.unmodifiableMap(wordMap);
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

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Document o) {
        long timeDif = lastUseTime - o.getLastUseTime();
        if (timeDif > 0) {
            return 1;
        } else if (timeDif < 0) {
            return -1;
        } else {
            return 0;
        }
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