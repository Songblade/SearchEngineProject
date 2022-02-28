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
        validateURI(uri);
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
        validateURI(uri);
        if (binaryData == null) {
            throw new IllegalArgumentException("binary data is null");
        }
        if (binaryData.length == 0) {
            throw new IllegalArgumentException("binary data is empty");
        }
        this.uri = uri;
        this.binaryData = Arrays.copyOf(binaryData, binaryData.length);
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

}