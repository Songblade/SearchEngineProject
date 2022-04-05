package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.stage3.*;
import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;

public class DocumentStoreImpl implements DocumentStore {
    // must use HashTableImpl to store documents
    private HashTable<URI, Document> table;
    private Stack<Undoable> commandStack;

    //This shouldn't do much, just set up the HashTable
    public DocumentStoreImpl() {
        table = new HashTableImpl<>();
        commandStack = new StackImpl<>();
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

        //this part deals with the adding the command to the stack
        //commandStack.push(new Command(uri, () -> ()));
        // since I need the Command added to add the old doc
        int oldHash; // the old hashcode, to be returned
        if (table.get(uri) == null) { // I must find it now, because if it is a delete, I will be returning soon
            oldHash = 0; // I don't want problems getting the hashcode of a null element
        } else {
            oldHash = table.get(uri).hashCode();
        }
        // this adds the appropriate command to the command stack, along with saying what to add back

        Document previousDoc = table.get(uri);
        commandStack.push(new GenericCommand<URI>(uri, (uri1) -> {
            // if previousDoc is null, HashTable will delete it for me
            table.put(uri, previousDoc);
            return true;
        }));

        if (input == null) { // deleting the document, if that is what was asked
            table.put(uri, null);
            return oldHash;
        }

        // everything from here on in should only happen if I have a valid document
        table.put(uri, readDataToDocument(input, uri, format));
        return oldHash;
    }

    // So I don't have to worry about having a putDocument method too long, I am moving the code that gets a
    // document from the input stream to a separate method
    private Document readDataToDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
        /*Your code will receive documents as an InputStream and the document’s key as an instance of URI. When a document
        is added to your document store, you must do the following:
        a. Read the entire contents of the document from the InputStream into a byte[]
        b. Create an instance of DocumentImpl with the URI and the String or byte[]that was passed to you.
        c. Insert the Document object into the hash table with URI as the key and the Document object as the value
        d. Return the hashCode of the previous document that was stored in the hashTable at that URI, or zero if there was
        none*/
        byte[] data = input.readAllBytes(); // making the array just big enough to read all the data
        if (format == DocumentFormat.TXT) {
            return new DocumentImpl(uri, new String(data));
        } else { // if the format is binary
            return new DocumentImpl(uri, data);
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
            return false; // not deleting anything, because nothing to delete
        }
        Document previousDoc = table.get(uri);
        commandStack.push(new GenericCommand<URI>(uri, (uri1) -> {
            // if previousDoc is null, HashTable will delete it for me
            table.put(uri, previousDoc);
            return true;
        }));
        // this has to be after adding the command to the stack, because it is still supposed to add a command
        // even if it does nothing
        if (table.get(uri) == null) {
            return false;
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
        if (commandStack.peek() == null) {
            throw new IllegalStateException("No commands to undo");
        }
        // I remove the top command and undo it, and hope my lambda stuff works
        commandStack.pop().undo();
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     *
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    @Override
    public void undo(URI uri) throws IllegalStateException {
        if (commandStack.peek() == null) {
            throw new IllegalStateException("No commands to undo");
        }
        Stack<Undoable> helperStack = new StackImpl<>(); // to put stuff on
        // I go through each command on the stack and examine it
        // if it is not the command I am looking for, I put it on helperStack
        // if it is the command I am looking for, I activate it
        // if I never find the command, I throw an ISE
        // After activating it or before leaving with the ISE, I put all the stuff from the helperStack back on the main stack
        GenericCommand<URI> command;
        boolean undone = false;
        try {
            do { // I don't need a do-while, a regular while would do, but I ended up with this and see no problems
                command = (GenericCommand<URI>) commandStack.pop();
                if (command == null) {
                    throw new IllegalStateException("No commands with the uri \"" + uri + "\" to be undone");
                }
                if (command.getTarget().equals(uri)) { // if we found our command
                    command.undo();
                    undone = true;
                } else { // if this is a dud that is not getting undone
                    helperStack.push(command);
                }
            } while (!undone);
        } finally {
            restackStack(helperStack);
        }

    }

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE INSENSITIVE.
     *
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> search(String keyword) {
        return null;
    }

    /**
     * Retrieve all documents whose text starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE INSENSITIVE.
     *
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        return null;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     *
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String keyword) {
        return null;
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE INSENSITIVE.
     *
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        return null;
    }

    // this method is called at the end of undo(URI) to put everything back on the stack
    private void restackStack(Stack<Undoable> helperStack) {
        Undoable command;
        do { // not regular while loop, because command could be null the first time from before, and
            // that should not cause any problems
            command = helperStack.pop();
            if (command != null) { // if this is the first command that was undone, it will be null
                commandStack.push(command);
            }
        } while (command != null);
    }
}