package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.stage2.*;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

public class DocumentStoreImpl implements DocumentStore {
    // must use HashTableImpl to store documents
    private HashTable<URI, Document> table;

    //This shouldn't do much, just set up the HashTable
    public DocumentStoreImpl() {
        table = new HashTableImpl<>();
    }

    /**
     * @param input  the document being put
     * @param uri    unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc. If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException              if there is an issue reading input
     * @throws IllegalArgumentException if uri or format are null
     */
    @Override
    public int putDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
        // to anyone reading this, this is not a monster method, because it is a large percentage
        // whitespace and comments to structure and explain it
        // I counted 27-28 lines of actual code, include single "}"s, when the limit is 30
        if (uri == null) {
            throw new IllegalArgumentException("URI is null");
        }
        if (input != null && format == null) { // if input is null, then this is a delete, and the format
            // doesn't matter
            throw new IllegalArgumentException("format is null");
        }

        int oldHash; // the old hashcode, to be returned
        if (table.get(uri) == null) { // I must find it now, because if it is a delete, I will be returning soon
            oldHash = 0; // I don't want problems getting the hashcode of a null element
        } else {
            oldHash = table.get(uri).hashCode();
        }

        if (input == null) { // deleting the document, if that is what was asked
            table.put(uri, null);
            return oldHash;
        }

        // everything from here on in should only happen if I have a valid document
        /*Your code will receive documents as an InputStream and the document’s key as an instance of URI. When a document
        is added to your document store, you must do the following:
        a. Read the entire contents of the document from the InputStream into a byte[]
        b. Create an instance of DocumentImpl with the URI and the String or byte[]that was passed to you.
        c. Insert the Document object into the hash table with URI as the key and the Document object as the value
        d. Return the hashCode of the previous document that was stored in the hashTable at that URI, or zero if there was
        none*/
        byte[] data = input.readAllBytes(); // making the array just big enough to read all the data
        Document doc;
        if (format == DocumentFormat.TXT) {
            doc = new DocumentImpl(uri, new String(data));
        } else { // if the format is binary
            doc = new DocumentImpl(uri, data);
        }
        table.put(uri, doc);
        return oldHash;
    }


    /**
     * @param uri the unique identifier of the document to get
     * @return the given document
     */
    @Override
    public Document getDocument(URI uri) {
        return table.get(uri); // if the value is null, this will return null
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    @Override
    public boolean deleteDocument(URI uri) {
        if (uri == null || table.get(uri) == null) {
            return false; // not deleting anything, because nothing to delete
        }
        // if there is something to delete
        table.put(uri, null);
        return true;
    }

    /**
     * undo the last put or delete command
     *
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    @Override
    public void undo() throws IllegalStateException {

    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     *
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    @Override
    public void undo(URI uri) throws IllegalStateException {

    }
}