package edu.yu.cs.com1320.project.stage1.impl;

import edu.yu.cs.com1320.project.stage1.*;
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
        byte[] data = getData(input); // making the array just big enough to read all the data
        Document doc;
        if (format == DocumentFormat.TXT) {
            doc = new DocumentImpl(uri, new String(data));
        } else { // if the format is binary
            doc = new DocumentImpl(uri, data);
        }
        table.put(uri, doc);
        return oldHash;
    }

    // this private method gets the data from the input in putDocument() and returns it in byte form
    // I need a special method for this because otherwise, I can't be sure that I will create a byte long enough
    // to read all the data in InputStream
    // plus, depending on the type of InputStream, more data might come after I start reading it
    // And I don't want extra 0 data at the end
    private byte[] getData(InputStream input) throws IOException {
        int index = 0;
        byte[] data = new byte[input.available()]; // hopefully, this is big enough, but if not, I can resize it later
        while (true) {
            // First, I get the read data, and check that it isn't -1
            // if it is, if index is 0, I throw an error, because the data is empty
            // Otherwise, I return the data, we have our byte array
            // If it isn't -1, I cast it to a byte and add it to the array
            // Increment the counter
            // If the array has reached the max, I increase it to the current size + whatever available says now
            int readData = input.read();
            if (readData == -1) {
                if (index == 0) {
                    throw new IllegalArgumentException("InputStream has no data");
                } else { // if this is not the first byte of data
                    input.close(); // does nothing for ByteStream, but for other types, which I see no reason
                        // not to service, this frees up resources
                    if (index != data.length - 1) { // this should never happen, but I don't want trailing 0s
                        data = Arrays.copyOf(data, index); // up to but not including the current index,
                            // which has -1, so we don't want that one
                    }
                    return data; // we are done reading
                }
            } // if readData() is not -1 and is instead actual data
            data[index] = (byte) (readData); // this is the trickiest part, I am subtracting to make
                // sure that this remains within the bounds of a byte
                // but I am not entirely sure if this is how InputStreams work, my tests will catch it
                // I got rid of - 128, hopefully that doesn't break anything
            index++;
            if (index == data.length) { // if we reached the end of the array
                data = Arrays.copyOf(data, data.length + input.available());
            }
        }
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
        if (uri == null) {
            throw new IllegalArgumentException("URI is null");
        }

        if (table.get(uri) == null) {
            return false; // not deleting anything, because nothing to delete
        } // if there is something to delete

        table.put(uri, null);
        return true;
    }
}