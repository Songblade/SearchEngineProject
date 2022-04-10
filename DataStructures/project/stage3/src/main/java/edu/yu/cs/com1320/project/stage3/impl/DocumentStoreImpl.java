package edu.yu.cs.com1320.project.stage3.impl;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.stage3.*;
import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class DocumentStoreImpl implements DocumentStore {
    // must use HashTableImpl to store documents
    private HashTable<URI, Document> table;
    private Stack<Undoable> commandStack;
    private Trie<Document> searchTrie;

    //This shouldn't do much, just set up the HashTable
    public DocumentStoreImpl() {
        table = new HashTableImpl<>();
        commandStack = new StackImpl<>();
        searchTrie = new TrieImpl<>();
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

        // I must find it now, because if it is a delete, I will be returning soon
        int oldHash = getOldHashCode(uri); // the old hashcode, to be returned

        if (input == null) { // deleting the document, if that is what was asked
            deleteDocument(uri); // using this method, so that the undo will work properly
            return oldHash;
        }

        // everything from here on in should only happen if I have a valid document
        Document doc = readDataToDocument(input, uri, format);

        //this part deals with the adding the command to the stack
        // since I need the Command added to add the old doc
        Document previousDoc = table.get(uri);
        commandStack.push(new GenericCommand<>(uri, (uri1) -> {
            // if previousDoc is null, HashTable will delete it for me
            removeDocFromTrie(doc);
            table.put(uri, previousDoc);
            putWordsInTrie(previousDoc);
            return true;
        }));

        table.put(uri, doc);
        putWordsInTrie(doc);
        return oldHash;
    }

    // this method returns the hash code of whatever document putDocument will be replacing
    private int getOldHashCode(URI uri) {
        int oldHash; // the old hashcode, to be returned
        if (table.get(uri) == null) {
            oldHash = 0; // I don't want problems getting the hashcode of a null element
        } else {
            oldHash = table.get(uri).hashCode();
        }
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

    private void putWordsInTrie(Document doc) {
        if (doc == null) {
            return;
        }
        for (String word : doc.getWords()) {
            searchTrie.put(word, doc);
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
        commandStack.push(new GenericCommand<>(uri, (uri1) -> {
            // if previousDoc is null, HashTable will delete it for me
            table.put(uri, previousDoc);
            putWordsInTrie(previousDoc);
            return true;
        }));
        // this has to be after adding the command to the stack, because it is still supposed to add a command
        // even if it does nothing
        if (previousDoc == null) {
            return false;
        }
        // if there is something to delete
        removeDocFromTrie(previousDoc);
        table.put(uri, null);
        return true;
    }

    private void removeDocFromTrie(Document doc) {
        if (doc == null) {
            return;
        }
        for (String word : doc.getWords()) {
            searchTrie.delete(word, doc);
        }
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
        Undoable command;
        boolean undone = false;
        try {
            do { // I don't need a do-while, a regular while would do, but I ended up with this and see no problems
                command = commandStack.pop();
                if (command == null) {
                    throw new IllegalStateException("No commands with the uri \"" + uri + "\" to be undone");
                } else if (command instanceof GenericCommand) { // if this is a single command
                    undone = checkAndDeleteGenericCommand((GenericCommand<URI>) command, uri, helperStack);
                } else if (command instanceof CommandSet) {
                    undone = checkAndDeleteCommandSet((CommandSet<URI>) command, uri, helperStack);
                }
            } while (!undone);
        } finally {
            restackStack(helperStack);
        }

    }

    // I am making this generic because I can, even though I will only ever use it with URI
    private <T> boolean checkAndDeleteGenericCommand(GenericCommand<T> command, T uri, Stack<Undoable> helperStack) {
        if (command.getTarget().equals(uri)) { // if we found our command
            command.undo();
            return true;
        }
        // if this is a dud that is not getting undone
        helperStack.push(command);
        return false;
    }

    private <T> boolean checkAndDeleteCommandSet(CommandSet<T> commandSet, T uri, Stack<Undoable> helperStack) {
        if (commandSet.containsTarget(uri)) { // if we found our command
            commandSet.undo(uri);
            // if there are still more commands in it, put it back in the commandStack
            // so it can be undone from again in the future
            if (commandSet.size() > 0) {
                commandStack.push(commandSet);
            }
            return true;
        }
        // if this is a dud that is not getting undone
        helperStack.push(commandSet);
        return false;
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
        return searchTrie.getAllSorted(cleanKey(keyword), new DocComparator(keyword));
    }

    // this method turns the key lowercase and gets rid of any symbols
    private String cleanKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key is null");
        }
        String newKey = "";
        // the following loop makes sure the key only contains alphanumerics
        for (int i = 0; i < key.length(); i++) {
            if (Character.getType(key.charAt(i)) == Character.UPPERCASE_LETTER) {
                newKey += (Character.toLowerCase(key.charAt(i)));
                // so that it will be lowercase
            }
            if (Character.getType(key.charAt(i)) == Character.LOWERCASE_LETTER ||
                    Character.getType(key.charAt(i)) == Character.DECIMAL_DIGIT_NUMBER) {
                newKey += (Character.toLowerCase(key.charAt(i)));
            }
            // if it isn't a letter or a number, it isn't added
        }
        return newKey;
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
        return searchTrie.getAllWithPrefixSorted(cleanKey(keywordPrefix), new PrefixComparator(keywordPrefix));
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     *
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<Document> deleted = searchTrie.deleteAll(cleanKey(keyword));
        removeDocs(deleted);
        return extractURIs(deleted);
    }

    // this method deletes a set of documents from both the hashtable and trie
    // it also creates a CommandSet to undo
    private void removeDocs(Set<Document> docs) {
        CommandSet<URI> undoActions = new CommandSet<>();
        for (Document doc : docs) {
            // we create an undo for the document, which will add it back to both the HashTable and the Trie
            undoActions.addCommand(new GenericCommand<>(doc.getKey(), uri1 -> {
                table.put(doc.getKey(), doc);
                putWordsInTrie(doc);
                return true;
            }));

            // we delete the doc from our document memory and our word memory
            removeDocFromTrie(doc);
            table.put(doc.getKey(), null);
        }
        commandStack.push(undoActions);
    }

    // this turns a Set of Documents to a set of their URIs
    // There has got to be a way to do this with streams, but I can't figure it out
    private Set<URI> extractURIs(Set<Document> docSet) {
        Set<URI> returnSet = new HashSet<>();
        for (Document doc : docSet) {
            returnSet.add(doc.getKey());
        }
        return returnSet;
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
        Set<Document> deleted = searchTrie.deleteAllWithPrefix(cleanKey(keywordPrefix));
        removeDocs(deleted);
        return extractURIs(deleted);
    }

}

// this comparator compares documents by number of a certain word
class DocComparator implements Comparator<Document> {

    private String keyWord; // this is the word that we compare by

    public DocComparator(String keyWord) {
        this.keyWord = keyWord;
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.
     *
     * The implementor must ensure that {@code sgn(compare(x, y)) ==
     * -sgn(compare(y, x))} for all {@code x} and {@code y}.  (This
     * implies that {@code compare(x, y)} must throw an exception if and only
     * if {@code compare(y, x)} throws an exception.)
     *
     * The implementor must also ensure that the relation is transitive:
     * {@code ((compare(x, y)>0) && (compare(y, z)>0))} implies
     * {@code compare(x, z)>0}.
     *
     * Finally, the implementor must ensure that {@code compare(x, y)==0}
     * implies that {@code sgn(compare(x, z))==sgn(compare(y, z))} for all
     * {@code z}.
     *
     * It is generally the case, but <i>not</i> strictly required that
     * {@code (compare(x, y)==0) == (x.equals(y))}.  Generally speaking,
     * any comparator that violates this condition should clearly indicate
     * this fact.  The recommended language is "Note: this comparator
     * imposes orderings that are inconsistent with equals."
     *
     * In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the
     * second.
     * @throws NullPointerException if an argument is null and this
     *                              comparator does not permit null arguments
     * @throws ClassCastException   if the arguments' types prevent them from
     *                              being compared by this comparator.
     */
    @Override
    public int compare(Document o1, Document o2) {
        if (o1 == null || o2 == null) {
            throw new NullPointerException();
        }
        // I think that normally, it should be the opposite, as this is saying that o1 is smaller than o2 when it is actually greater
        // But this makes sense, because we want it in reverse order
        return o2.wordCount(keyWord) - o1.wordCount(keyWord);
    }
}

// this comparator compares documents by number of a certain prefix
class PrefixComparator implements Comparator<Document> {

    private String prefix; // this is the prefix that we compare by

    public PrefixComparator(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.
     *
     * The implementor must ensure that {@code sgn(compare(x, y)) ==
     * -sgn(compare(y, x))} for all {@code x} and {@code y}.  (This
     * implies that {@code compare(x, y)} must throw an exception if and only
     * if {@code compare(y, x)} throws an exception.)
     *
     * The implementor must also ensure that the relation is transitive:
     * {@code ((compare(x, y)>0) && (compare(y, z)>0))} implies
     * {@code compare(x, z)>0}.
     *
     * Finally, the implementor must ensure that {@code compare(x, y)==0}
     * implies that {@code sgn(compare(x, z))==sgn(compare(y, z))} for all
     * {@code z}.
     *
     * It is generally the case, but <i>not</i> strictly required that
     * {@code (compare(x, y)==0) == (x.equals(y))}.  Generally speaking,
     * any comparator that violates this condition should clearly indicate
     * this fact.  The recommended language is "Note: this comparator
     * imposes orderings that are inconsistent with equals."
     *
     * In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the
     * second.
     * @throws NullPointerException if an argument is null and this
     *                              comparator does not permit null arguments
     * @throws ClassCastException   if the arguments' types prevent them from
     *                              being compared by this comparator.
     */
    @Override
    public int compare(Document o1, Document o2) {
        if (o1 == null || o2 == null) {
            throw new NullPointerException();
        }
        // I think that normally, it should be the opposite, as this is saying that o1 is smaller than o2 when it is actually greater
        // But this makes sense, because we want it in reverse order
        return prefixCount(o2) - prefixCount(o1);
    }

    // this returns the number of times the prefix is in the document
    private int prefixCount(Document doc) {
        int count = 0;
        for (String word : doc.getWords()) {
            if (word.startsWith(prefix)) { // if the word starts with the prefix
                count += doc.wordCount(word);
            }
        }
        return count;
    }
}