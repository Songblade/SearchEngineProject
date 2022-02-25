package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.Document;

import java.net.URI;
import java.util.Arrays;

public class DocumentImpl implements Document {

    private URI uri; // the URI of the document
    private String text; // the text of the document, null if not applicable
    private byte[] binaryData; // the data of the document, null if not applicable

    // constructor that uses text
    public DocumentImpl(URI uri, String text) {
        if (uri == null) {
            throw new IllegalArgumentException("URI is null");
        }
        if (text == null) {
            throw new IllegalArgumentException("text is null");
        } // Piazza says we don't need to worry about a blank URI
        if (text.equals("")) {
            throw new IllegalArgumentException("text is blank");
        }
        this.uri = uri;
        this.text = text;
    }

    public DocumentImpl(URI uri, byte[] binaryData) {
        if (uri == null) {
            throw new IllegalArgumentException("URI is null");
        }
        if (binaryData == null) {
            throw new IllegalArgumentException("binary data is null");
        }
        if (binaryData.length == 0) {
            throw new IllegalArgumentException("binary data is empty");
        }
        this.uri = uri;
        this.binaryData = Arrays.copyOf(binaryData, binaryData.length);
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