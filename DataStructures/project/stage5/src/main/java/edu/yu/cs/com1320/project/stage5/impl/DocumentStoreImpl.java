package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.Stack;
import edu.yu.cs.com1320.project.stage5.*;
import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class DocumentStoreImpl implements DocumentStore {
    // must use HashTableImpl to store documents
    private final BTree<URI, Document> storeTree;
    private final Stack<Undoable> commandStack;
    private final Trie<URI> searchTrie;
    private final MinHeap<DocShell> memoryHeap;
    private final Set<URI> docsOnDisk; // a set of documents that are in the disk
    private int maxDocCount = -1; // maximum number of docs allowed, -1 means no limit
    private int maxDocBytes = -1; // maximum number of doc bytes allowed, -1 means no limit
    private int docCount; // current number of docs
    private int docBytes; // current number of doc bytes

    private static class DocShell implements Comparable<DocShell> {

        private final URI uri;
        private final BTree<URI, Document> tree;

        private DocShell(URI uri, BTree<URI, Document> tree) {
            this.uri = uri;
            this.tree = tree;
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
        public int compareTo(DocShell o) {
            return tree.get(this.uri).compareTo(tree.get(o.uri)); // gets the docs and calls their compareTo
        } // we were told this was really inefficient but what we had to do

        // overriding equals should make reheapify easier, as I can just make a new DocShell and say reheapify
        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (this == o) {
                return true;
            }
            if (this.getClass() != o.getClass()) {
                return false;
            }
            // equality is determined by URI, once same class
            return this.uri == ((DocShell)o).uri;
        }
    }

    //This shouldn't do much, just set up the various fields
    public DocumentStoreImpl() {
        this(null);
    }

    public DocumentStoreImpl(File baseDir) {
        storeTree = new BTreeImpl<>();
        commandStack = new StackImpl<>();
        searchTrie = new TrieImpl<>();
        memoryHeap = new MinHeapImpl<>();
        storeTree.setPersistenceManager(new DocumentPersistenceManager(baseDir));
        docsOnDisk = new HashSet<>();
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
        // making the document, and throwing exceptions if necessary
        Document doc = vetAndMakeDocument(input, uri, format);
        // gets the old doc's hashcode to be returned, or 0 if there is no old document
        int oldHash = getOldHashCode(uri);
        if (doc == null) { // this is actually a delete
            deleteDocument(uri); // using this method, so that the undo will work properly
            return oldHash;
        }

        // everything from here on in should only happen if I have a valid document

        // if there is a previous doc, we remove it from the trie and heap
        // I'm honestly not sure how no one caught the problem of never removing old docs from tries
        if (storeTree.get(uri) != null) {
            removeDocFromTrie(storeTree.get(uri));
            if (!docsOnDisk.contains(uri)) {
                removeDocFromHeap(storeTree.get(uri));
            }
        }

        //this part deals with the adding the command to the stack
        // since I need the Command added to add the old doc
        Document previousDoc = storeTree.get(uri);
        boolean wasOnDisk = docsOnDisk.contains(uri);
        commandStack.push(new GenericCommand<>(uri, (uri1) -> {
            // if previousDoc is null, HashTable will delete it for me
            removeDocFromTrie(doc);
            removeDocFromHeap(doc);
            storeTree.put(uri, previousDoc);
            if (previousDoc != null) {
                putWordsInTrie(previousDoc);
                try {
                    if (wasOnDisk) {
                        storeTree.moveToDisk(uri);
                    } else {
                        addDocToHeap(previousDoc);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }));

        storeTree.put(uri, doc);
        addDocToHeap(doc);
        putWordsInTrie(doc);
        return oldHash;
    }

    /**
     * Makes sure the document fits every requirement, and then returns it
     * @param input stream for the document's data
     * @param uri address for the document
     * @param format of the document, TXT or BINARY
     * @return the document, if all the inputs are valid, or null, if this is a delete
     */
    private Document vetAndMakeDocument(InputStream input, URI uri, DocumentFormat format) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("URI is null");
        }
        if (input != null && format == null) { // if input is null, then this is a delete, and the format
            // doesn't matter
            throw new IllegalArgumentException("format is null");
        }

        if (input == null) { // deleting the document, if that is what was asked
            return null;
        }

        // everything from here on in should only happen if I have a valid document

        Document doc = readDataToDocument(input, uri, format);
        if (maxDocBytes != -1 && getByteLength(doc) > maxDocBytes) {
            throw new IllegalArgumentException("document size " + getByteLength(doc) + " bytes is greater than limit of " + maxDocBytes);
        }
        return doc;
    }

    // this method returns the hash code of whatever document putDocument will be replacing
    private int getOldHashCode(URI uri) {
        return storeTree.get(uri) == null ? 0 : storeTree.get(uri).hashCode();
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
            return new DocumentImpl(uri, new String(data), null);
        } else { // if the format is binary
            return new DocumentImpl(uri, data);
        }
    }

    private void putWordsInTrie(Document doc) {
        if (doc == null) {
            return;
        }
        for (String word : doc.getWords()) {
            searchTrie.put(word, doc.getKey());
        }
    }

    /**
     * Does all the memoryHeap stuff in putDocument, like adding it to the heap and overflowing memory
     * I no longer throw an error here if the doc is too big to fit
     * @param doc being added to the DocumentStore
     */
    private void addDocToHeap(Document doc) throws IOException {
        int byteLength = getByteLength(doc);
        doc.setLastUseTime(System.nanoTime());
        docCount++;
        docBytes += byteLength; // gets whatever the byte length is
        memoryHeap.insert(new DocShell(doc.getKey(), storeTree));
        while ((maxDocCount != -1 && docCount > maxDocCount) ||
                (maxDocBytes != -1 && docBytes > maxDocBytes)) {
            try {
                overflowMemory();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * @param doc whose byte length you are getting
     * @return the length of the byte stored in the doc, or corresponding to its string
     */
    private int getByteLength(Document doc) {
        return doc.getDocumentTxt() == null ? doc.getDocumentBinaryData().length :
                doc.getDocumentTxt().getBytes().length;
    }

    /**
     * Removes every trace of the least-recently used doc from wherever it can be found
     */
    private void overflowMemory() throws Exception {
        // removes from memory heap
        Document deletedDoc = storeTree.get(memoryHeap.remove().uri);
        // removes from hashtable
        storeTree.moveToDisk(deletedDoc.getKey());
        docsOnDisk.add(deletedDoc.getKey());
        // lowers the doc and byte totals
        docCount--;
        docBytes -= getByteLength(deletedDoc);
    }

    /**
     * This method removes a document from the heap
     * @param doc to be removed
     */
    private void removeDocFromHeap(Document doc) {
        if (docsOnDisk.contains(doc.getKey())) { // should stop problems from removing docs
            return;                             // not actually in the heap
        }
        Stack<DocShell> helperStack = new StackImpl<>(); // to store docs
        DocShell foundDoc = memoryHeap.remove();
        while (!doc.getKey().equals(foundDoc.uri)) {
            helperStack.push(foundDoc);
            foundDoc = memoryHeap.remove();
        }
        // at this point, we have found the right doc, so we put everything else back
        while (helperStack.peek() != null) {
            memoryHeap.insert(helperStack.pop());
        }
        // show that the doc has been removed
        docCount--;
        docBytes -= getByteLength(doc);
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document
     */
    @Override
    public Document getDocument(URI uri) {
        Document doc = storeTree.get(uri);
        if (doc != null) {
            updateDocTime(doc);
        }
        return doc; // if the value is null, this will return null
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
        Document previousDoc = storeTree.get(uri);
        boolean wasOnDisk = docsOnDisk.contains(uri);
        commandStack.push(new GenericCommand<>(uri, (uri1) -> {
            // I don't have to worry about taking out what was previous, because this is delete
            // I need to figure out what to do if the doc is too big
            if (maxDocBytes != -1 && getByteLength(previousDoc) > maxDocBytes) {
                throw new IllegalArgumentException("document size " + getByteLength(previousDoc) + " bytes is greater than limit of " + maxDocBytes);
            }
            // if previousDoc is null, HashTable will delete it for me
            storeTree.put(uri, previousDoc);
            try {
                if (wasOnDisk) {
                    storeTree.moveToDisk(uri);
                } else {
                    addDocToHeap(previousDoc);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        if (!docsOnDisk.contains(uri)) { // only remove it if it was in local memory and so on the heap
            removeDocFromHeap(previousDoc); // should no longer be necessary
        }
        storeTree.put(uri, null);
        return true;
    }

    private void removeDocFromTrie(Document doc) {
        if (doc == null) {
            return;
        }
        for (String word : doc.getWords()) {
            searchTrie.delete(word, doc.getKey());
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
     * @param uri being undone
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
    // if this is the right command, we delete it, otherwise, we put it on the helper stack
    // undoing in the process if appropriate
    private <T> boolean checkAndDeleteGenericCommand(GenericCommand<T> command, T uri, Stack<Undoable> helperStack) {
        if (command.getTarget().equals(uri)) { // if we found our command
            command.undo();
            return true;
        }
        // if this is a dud that is not getting undone
        helperStack.push(command);
        return false;
    }

    // if the command set has the right command, we delete the command (and maybe the entire set), if not, we put it
    // on the helper stack
    // undoing also if appropriate
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
     * @param keyword being searched for
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> search(String keyword) {
        List<URI> results = searchTrie.getAllSorted(cleanKey(keyword), new DocComparator(keyword, storeTree));
        List<Document> docs = new ArrayList<>();
        for (URI uri : results) { // we take the uri, get its doc, update the time, and add it to the return list
            Document doc = storeTree.get(uri);
            updateDocTime(doc);
            docs.add(doc);
        }
        return docs;
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
     * @param keywordPrefix being searched for
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {
        List<URI> results = searchTrie.getAllWithPrefixSorted(cleanKey(keywordPrefix), new PrefixComparator(keywordPrefix, storeTree));
        List<Document> docs = new ArrayList<>();
        for (URI uri : results) {
            Document doc = storeTree.get(uri);
            updateDocTime(doc);
            docs.add(doc);
        }
        return docs;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     *
     * @param keyword whose docs are being deleted
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<URI> deleted = searchTrie.deleteAll(cleanKey(keyword));
        removeDocs(deleted);
        return deleted;
    }

    // this method deletes a set of documents from the B-Tree, heap, and trie
    // it also creates a CommandSet to undo
    private void removeDocs(Set<URI> uris) {
        CommandSet<URI> undoActions = new CommandSet<>();
        for (URI uri : uris) {
            Document doc = storeTree.get(uri);
            boolean wasOnDisk = docsOnDisk.contains(uri);
            // we create an undo for the document, which will add it back to both the HashTable and the Trie
            undoActions.addCommand(new GenericCommand<>(uri, uri1 -> {
                storeTree.put(uri, doc);
                putWordsInTrie(doc);
                try {
                    if (wasOnDisk) {
                        storeTree.moveToDisk(uri);
                    } else {
                        addDocToHeap(doc);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false; // since I can't throw an exception, I will allow for this
                }
                return true;
            }));

            // we delete the doc from our document memory and our word memory
            if (!docsOnDisk.contains(uri)) { // the if should no longer be necessary
                removeDocFromHeap(doc);
            }
            removeDocFromTrie(doc);
            storeTree.put(doc.getKey(), null);
        }
        commandStack.push(undoActions);
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE INSENSITIVE.
     *
     * @param keywordPrefix whose docs are being deleted
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        Set<URI> deleted = searchTrie.deleteAllWithPrefix(cleanKey(keywordPrefix));
        removeDocs(deleted);
        return deleted;
    }

    /**
     * set maximum number of documents that may be stored
     *
     * @param limit of documents that can be stored
     */
    @Override
    public void setMaxDocumentCount(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit " + limit + " documents is too small");
        }
        maxDocCount = limit;
        while (docCount > maxDocCount) {
            try {
                overflowMemory();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * set maximum number of bytes of memory that may be used by all the documents in memory combined
     *
     * @param limit of bytes that can have documents stored
     */
    @Override
    public void setMaxDocumentBytes(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit " + limit + " bytes is too small");
        }
        maxDocBytes = limit;
        while (docBytes > maxDocBytes) {
            try {
                overflowMemory();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates the document's time to the current time, to be used when using it
     * Or adds it to the heap if it was in memory
     * @param doc to be updated
     */
    private void updateDocTime(Document doc) {
        if (docsOnDisk.contains(doc.getKey())) {
            try {
                addDocToHeap(doc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            docsOnDisk.remove(doc.getKey());
        } else {
            doc.setLastUseTime(System.nanoTime());
            memoryHeap.reHeapify(new DocShell(doc.getKey(), storeTree));
        }
    }

}

// this comparator compares documents by number of a certain word
class DocComparator implements Comparator<URI> {

    private final String keyWord; // this is the word that we compare by
    private final BTree<URI, Document> tree;

    public DocComparator(String keyWord, BTree<URI, Document> tree) {
        this.keyWord = keyWord;
        this.tree = tree;
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
    public int compare(URI o1, URI o2) {
        if (o1 == null || o2 == null) {
            throw new NullPointerException();
        }
        // I think that normally, it should be the opposite, as this is saying that o1 is smaller than o2 when it is actually greater
        // But this makes sense, because we want it in reverse order
        return tree.get(o2).wordCount(keyWord) - tree.get(o1).wordCount(keyWord);
    }
}

// this comparator compares documents by number of a certain prefix
class PrefixComparator implements Comparator<URI> {

    private final String prefix; // this is the prefix that we compare by
    private final BTree<URI, Document> tree;

    public PrefixComparator(String prefix, BTree<URI, Document> tree) {
        this.prefix = prefix;
        this.tree = tree;
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
    public int compare(URI o1, URI o2) {
        if (o1 == null || o2 == null) {
            throw new NullPointerException();
        }
        // I think that normally, it should be the opposite, as this is saying that o1 is smaller than o2 when it is actually greater
        // But this makes sense, because we want it in reverse order
        return prefixCount(tree.get(o2)) - prefixCount(tree.get(o1));
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