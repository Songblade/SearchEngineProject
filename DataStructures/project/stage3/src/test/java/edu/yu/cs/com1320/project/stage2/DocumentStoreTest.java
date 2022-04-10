package edu.yu.cs.com1320.project.stage2;

import edu.yu.cs.com1320.project.stage3.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import edu.yu.cs.com1320.project.stage3.impl.*;
import edu.yu.cs.com1320.project.stage3.DocumentStore.DocumentFormat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashSet;

public class DocumentStoreTest {
    public DocumentStoreTest(){}
    // I will not test for IOException since I have no idea what causes it and I don't think it has anything
    // to do with my methods, but with InputStream
    // I will also not test the underlying HashTable, because that got its own tests

    // Unfortunately, the Document.equals() checks if the hashCode is equal, which doesn't really tell me
    // that these are actually the same documents, so I will also check all the other data segments
    private void testDocumentEquality(Document expected, Document docReturn) {
        assertEquals(expected, docReturn);
        assertEquals(expected.getDocumentTxt(), docReturn.getDocumentTxt());
        assertArrayEquals(expected.getDocumentBinaryData(), docReturn.getDocumentBinaryData());
        assertEquals(expected.getKey(), docReturn.getKey());
    }

    // the following methods are so I can use the same group of documents without rewriting their code
    // for every test
    private byte[][] getByteData() {
        byte[][] bytes = new byte[6][1];
        for (int i = 0; i < 6; i++) {
            bytes[i][0] = (byte) i;
        }
        return bytes;
    }

    private InputStream[] getStreams() {
        InputStream[] streams = new InputStream[6];
        byte[][] bytes = getByteData();
        for (int i = 0; i < 6; i++) {
            streams[i] = new ByteArrayInputStream(bytes[i]);
        }
        return streams;
    }

    private URI[] getURIs() throws URISyntaxException {
        URI[] uris = {new URI("http://java.sun.com/index.html"), new URI("http://java.sun.com/outdex.html"),
                new URI("http://java.sun.com/insideoutdex.html"), new URI("http://java.sun.com/outsideindex.html"),
                new URI("http://java.sun.com/rightsideleftdex.html"), new URI("http://java.sun.com/pokedex.html")};
        return uris;
    }

    private Document[] getDocs() throws URISyntaxException {
        byte[][] bytes = getByteData();
        URI[] uris = getURIs();
        Document[] docs = {new DocumentImpl(uris[0], new String(bytes[0])), new DocumentImpl(uris[1], bytes[1]), new DocumentImpl(uris[2], new String(bytes[2])),
                new DocumentImpl(uris[3], bytes[3]), new DocumentImpl(uris[4], new String(bytes[4])), new DocumentImpl(uris[5], bytes[5])};
        return docs;
    }

    private DocumentStore getStore() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl();
        InputStream[] streams = getStreams();
        URI[] uris = getURIs();
        store.putDocument(streams[0], uris[0], DocumentFormat.TXT);
        store.putDocument(streams[1], uris[1], DocumentFormat.BINARY);
        store.putDocument(streams[2], uris[2], DocumentFormat.TXT);
        store.putDocument(streams[3], uris[3], DocumentFormat.BINARY);
        store.putDocument(streams[4], uris[4], DocumentFormat.TXT);
        store.putDocument(streams[5], uris[5], DocumentFormat.BINARY);
        return store;
    }



    // the following tests test putDocument(), though really also getDocument()

    // I want a test that adding 6 documents can still get all 6
    // This will involve both text and binary documents
    @Test
    public void lotsOfDocs() throws URISyntaxException, IOException {
        DocumentStore store = getStore();
        URI[] uris = getURIs();
        Document[] docs = getDocs();
        for (int i = 0; i < 6; i++) {
            testDocumentEquality(docs[i], store.getDocument(uris[i]));
        }
    }

    // test put returns 0 when first
    @Test
    public void putReturns0First() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl();
        byte[] bytes = {(byte) 0};
        InputStream stream = new ByteArrayInputStream(bytes);
        URI uri = new URI("http://java.sun.com/index.html");
        assertEquals(0, store.putDocument(stream, uri, DocumentFormat.BINARY));
    }

    // and then the previous doc hashCode when second
    @Test
    public void putReturnsHashCode() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl();
        byte[] bytes1 = {(byte) 0};
        InputStream stream1 = new ByteArrayInputStream(bytes1);
        URI uri1 = new URI("http://java.sun.com/index.html");
        store.putDocument(stream1, uri1, DocumentFormat.BINARY);
        Document doc = new DocumentImpl(uri1, bytes1);
        byte[] bytes2 = {(byte) 1};
        InputStream stream2 = new ByteArrayInputStream(bytes2);
        URI uri2 = new URI("http://java.sun.com/index.html");
        assertEquals(doc.hashCode(), store.putDocument(stream2, uri2, DocumentFormat.BINARY));
    }

    // test works with (very) long array
    @Test
    public void putWorksLong() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl();
        byte[] bytes = new byte[100000]; // hopefully this will stop maven from crashing
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) ((i - 127) % 128);
        }
        InputStream stream = new ByteArrayInputStream(bytes);
        URI uri = new URI("http://java.sun.com/index.html");
        store.putDocument(stream, uri, DocumentFormat.BINARY);
        Document doc = new DocumentImpl(uri, bytes);
        testDocumentEquality(doc, store.getDocument(uri));
    }

    // test throws IAE if URI is null
    @Test
    public void putIAENullURI() {
        DocumentStore store = new DocumentStoreImpl();
        byte[] bytes = {(byte) 0};
        InputStream stream = new ByteArrayInputStream(bytes);
        assertThrows(IllegalArgumentException.class, ()-> {
            store.putDocument(stream, null, DocumentFormat.BINARY);
        });
    }

    // test throws IAE if InputStream is null
    @Test
    public void putIAENullFormat() throws URISyntaxException {
        DocumentStore store = new DocumentStoreImpl();
        URI uri = new URI("http://java.sun.com/index.html");
        byte[] bytes = {(byte) 0};
        InputStream stream = new ByteArrayInputStream(bytes);
        assertThrows(IllegalArgumentException.class, () -> {
            store.putDocument(stream, uri, null);
        });
    }

    // test that put null actually deletes the document
    // I will also check that the format doesn't matter if you are deleting
    @Test
    public void putNullDeletes() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl();
        byte[] bytes = {(byte) 0};
        InputStream stream = new ByteArrayInputStream(bytes);
        URI uri = new URI("http://java.sun.com/index.html");
        store.putDocument(stream, uri, DocumentFormat.BINARY);
        store.putDocument(null, uri, null);
        assertNull(store.getDocument(uri));
    }
    // test that put null returns 0 if nothing there
    @Test
    public void putNullReturns0() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl();
        URI uri = new URI("http://java.sun.com/index.html");
        assertEquals(0, store.putDocument(null, uri, null));
    }
    // test that put null returns hashCode of deleted document
    @Test
    public void putNullReturnsHashCode() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl();
        byte[] bytes = {(byte) 0};
        InputStream stream = new ByteArrayInputStream(bytes);
        URI uri = new URI("http://java.sun.com/index.html");
        store.putDocument(stream, uri, DocumentFormat.BINARY);
        Document doc = new DocumentImpl(uri, bytes);
        assertEquals(doc.hashCode(), store.putDocument(null, uri, null));
    }

    // this test is for getDocument(), that on a null URI, it returns null
    @Test
    public void getNullReturnsNull() {
        DocumentStore store = new DocumentStoreImpl();
        assertNull(store.getDocument(null));
    }

    // the following tests are for deleteDocument()

    // delete actually deletes the document
    @Test
    public void deleteWorks() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl();
        byte[] bytes = {(byte) 0};
        InputStream stream = new ByteArrayInputStream(bytes);
        URI uri = new URI("http://java.sun.com/index.html");
        store.putDocument(stream, uri, DocumentFormat.BINARY);
        store.deleteDocument(uri);
        assertNull(store.getDocument(uri));
    }

    // delete returns false if no document was there
    @Test
    public void deleteEmptyReturnsFalse() throws URISyntaxException {
        DocumentStore store = new DocumentStoreImpl();
        URI uri = new URI("http://java.sun.com/index.html");
        assertFalse(store.deleteDocument(uri));
    }
    // delete return true if a document was deleted
    @Test
    public void deleteDocReturnsTrue() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl();
        byte[] bytes = {(byte) 0};
        InputStream stream = new ByteArrayInputStream(bytes);
        URI uri = new URI("http://java.sun.com/index.html");
        store.putDocument(stream, uri, DocumentFormat.BINARY);
        assertTrue(store.deleteDocument(uri));
    }
    // delete returns false if no document was there
    @Test
    public void deleteNullReturnsFalse() {
        DocumentStore store = new DocumentStoreImpl();
        assertFalse(store.deleteDocument(null));
    }

    // any tests after here are for undo()
    // undoing the first command
    @Test
    public void undoWorksFirst() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = new DocumentStoreImpl();
        byte[] bytes = {(byte) 0};
        InputStream stream = new ByteArrayInputStream(bytes);
        URI uri = new URI("http://java.sun.com/index.html");
        store.putDocument(stream, uri, DocumentFormat.BINARY);
        store.undo();
        assertNull(store.getDocument(uri));
    }

    // undoing a later command, but not the others
    @Test
    public void undoWorksLater() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = getStore();
        URI[] uris = getURIs();
        Document[] docs = getDocs();

        store.undo();
        assertNull(store.getDocument(uris[5]));
        testDocumentEquality(docs[4], store.getDocument(uris[4]));
    }

    // undoing all the commands
    @Test
    public void undoWorksForMultiple() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = getStore();
        URI[] uris = getURIs();
        for (int i = 0; i < 6; i++) {
            store.undo();
        }
        for (int i = 0; i < 6; i++) {
            assertNull(store.getDocument(uris[i]));
        }
    }

    // undoing a command where there was already a previous document gets you that previous document
    @Test
    public void undoWhenPrevious() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = getStore();
        URI[] uris = getURIs();
        InputStream[] streams = getStreams();
        Document[] docs = getDocs();

        store.putDocument(streams[4], uris[0], DocumentFormat.TXT);
        store.undo();
        testDocumentEquality(docs[0], store.getDocument(uris[0]));
    }

    // same as previous, but then also undoing the previous document (using the URI, so I don't have to change the test too much)
    @Test
    public void undoTwiceWhenPrevious() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = getStore();
        URI[] uris = getURIs();
        InputStream[] streams = getStreams();

        store.putDocument(streams[4], uris[0], DocumentFormat.TXT);
        store.undo();
        store.undo(uris[0]);
        assertNull(store.getDocument(uris[0]));
    }

    // undoing a URI when that is the only one in the HashTable
    @Test
    public void undoURIWorksFirst() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = new DocumentStoreImpl();
        byte[] bytes = {(byte) 0};
        InputStream stream = new ByteArrayInputStream(bytes);
        URI uri = new URI("http://java.sun.com/index.html");
        store.putDocument(stream, uri, DocumentFormat.BINARY);
        store.undo(uri);
        assertNull(store.getDocument(uri));
    }

    // undoing a URI multiple times
    @Test
    public void undoURITwiceWhenPrevious() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = getStore();
        URI[] uris = getURIs();
        InputStream[] streams = getStreams();

        store.putDocument(streams[4], uris[1], DocumentFormat.TXT);
        store.undo(uris[1]);
        store.undo(uris[1]);
        assertNull(store.getDocument(uris[1]));
    }

    // undo URI when previous
    @Test
    public void undoURIWhenPrevious() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = getStore();
        URI[] uris = getURIs();
        InputStream[] streams = getStreams();
        Document[] docs = getDocs();

        store.putDocument(streams[4], uris[0], DocumentFormat.TXT);
        store.undo(uris[0]);
        testDocumentEquality(docs[0], store.getDocument(uris[0]));
    }

    // undoing a URI when there are other documents above it
    @Test
    public void undoURIWhenBuried() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = getStore();
        URI[] uris = getURIs();

        store.undo(uris[2]);
        assertNull(store.getDocument(uris[2]));
    }

    // test that both undos work with deleteDocument as well
    @Test
    public void undoWorksDelete() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = getStore();
        URI[] uris = getURIs();
        Document[] docs = getDocs();

        store.deleteDocument(uris[0]);
        store.undo(uris[0]);
        testDocumentEquality(docs[0], store.getDocument(uris[0]));

        store.deleteDocument(uris[5]);
        store.undo();
        testDocumentEquality(docs[5], store.getDocument(uris[5]));
    }

    // check that if you have a null delete and another command below it, the second command isn't undone
        // until the first one is
    @Test
    public void undoDeleteNullObstructs() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = new DocumentStoreImpl();
        InputStream[] streams = getStreams();
        URI[] uris = getURIs();
        Document[] docs = getDocs();
        store.putDocument(streams[0], uris[0], DocumentFormat.TXT);
        store.deleteDocument(uris[1]);
        store.undo();
        testDocumentEquality(docs[0], store.getDocument(uris[0]));
        store.undo();
        assertNull(store.getDocument(uris[0]));
    }

    // undo throws ISE in both forms if there are no commands
    @Test
    public void undoEmptyISE() throws URISyntaxException {
        DocumentStore store = new DocumentStoreImpl();
        URI[] uris = getURIs();
        assertThrows(IllegalStateException.class, () -> store.undo());
        assertThrows(IllegalStateException.class, () -> store.undo(uris[0]));
    }

    // undo throws ISE in both forms if there was a command but it was already undone
    @Test
    public void undoEmptyISELater() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = new DocumentStoreImpl();
        byte[] bytes = {(byte) 0};
        InputStream stream = new ByteArrayInputStream(bytes);
        URI uri = new URI("http://java.sun.com/index.html");
        store.putDocument(stream, uri, DocumentFormat.BINARY);
        store.undo();
        assertThrows(IllegalStateException.class, () -> store.undo());
        assertThrows(IllegalStateException.class, () -> store.undo(uri));
    }

    // undo URI throws ISE when there are commands, but not for this URI
    @Test
    public void undoURIWrongDocISE() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = new DocumentStoreImpl();
        byte[] bytes = {(byte) 0};
        InputStream stream = new ByteArrayInputStream(bytes);
        URI[] uris = getURIs();
        store.putDocument(stream, uris[0], DocumentFormat.BINARY);
        assertThrows(IllegalStateException.class, () -> store.undo(uris[1]));
    }

    // testing that stack still works even after an ISE is thrown
    @Test
    public void undoURIWrongDocISERecovery() throws URISyntaxException, IOException, IllegalStateException {
        DocumentStore store = new DocumentStoreImpl();
        byte[] bytes = {(byte) 0};
        InputStream stream = new ByteArrayInputStream(bytes);
        URI[] uris = getURIs();
        Document[] docs = getDocs();
        store.putDocument(stream, uris[0], DocumentFormat.TXT);
        store.deleteDocument(uris[0]);
        try {
            store.undo(uris[1]);
        } catch (IllegalStateException e) {
            // nothing happens here
        }
        assertNull(store.getDocument(uris[1]));
        store.undo();
        testDocumentEquality(docs[0], store.getDocument(uris[0]));
    }
    // All my undo tests should still be valid, since undo's surface appearance hasn't changed, only how it
        // works on the inside

    private String[] newWords() {
        String[] words = new String[6];
        words[0] = "I love technology and I swim in it";
        words[1] = "All fear Big Tech. Their robots are about to overthrow us and replace us with tech minions with tech";
        words[2] = "How much tech could a techtech tech if a techtech could tech tech? ? Who cares.";
        words[3] = "The only thing we fear is Fear itself. The only thing Fear fears is your mom.";
        words[4] = "Not only does this say <tech> 3 times (some say technology is addictive) but it also includes techy 0s and other tech numbers :3";
        words[5] = "Fear fear fear fear fear tech tech tech tech tech binary doesn't care";
        return words;
        // tech: 0 has 0, 1 has 3, 2 has 4, 3 has 0, 4 has 2, 5 is binary
        // tech- (prefix): 0 has 1, 1 has 3, 2 has 6, 3 has 0, 4 has 4, 5 is binary
        // fear: 0 has 0, 1 has 1, 2 has 0, 3 has 4, 4 has 0, 5 is binary
    }

    // before I create any tests, I need a new set of Documents to work with
    private byte[][] newByteData() {
        String[] words = newWords();
        byte[][] bytes = new byte[6][1];
        for (int i = 0; i < 6; i++) {
            bytes[i] = words[i].getBytes();
        }
        return bytes;
    }

    private InputStream[] newStreams() {
        InputStream[] streams = new InputStream[6];
        byte[][] bytes = newByteData();
        for (int i = 0; i < 6; i++) {
            streams[i] = new ByteArrayInputStream(bytes[i]);
        }
        return streams;
    }

    private Document[] newDocs() throws URISyntaxException {
        String[] words = newWords();
        byte[][] bytes = newByteData();
        URI[] uris = getURIs();
        Document[] docs = {new DocumentImpl(uris[0], words[0]), new DocumentImpl(uris[1], words[1]), new DocumentImpl(uris[2], words[2]),
                new DocumentImpl(uris[3], words[3]), new DocumentImpl(uris[4], words[4]), new DocumentImpl(uris[5], bytes[5])};
        return docs;
    }

    private DocumentStore halfStore() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl();
        InputStream[] streams = newStreams();
        URI[] uris = getURIs();
        store.putDocument(streams[0], uris[0], DocumentFormat.TXT);
        store.putDocument(streams[1], uris[1], DocumentFormat.TXT);
        store.putDocument(streams[2], uris[2], DocumentFormat.TXT);
        return store;
    }

    private DocumentStore fullStore() throws URISyntaxException, IOException {
        DocumentStore store = halfStore();
        InputStream[] streams = newStreams();
        URI[] uris = getURIs();
        store.putDocument(streams[3], uris[3], DocumentFormat.TXT);
        store.putDocument(streams[4], uris[4], DocumentFormat.TXT);
        store.putDocument(streams[5], uris[5], DocumentFormat.BINARY);
        return store;
    }

    // Tests for search
    // Make sure search works, and in descending order
    // Make sure ignores case and symbols
    @Test
    public void searchWorksDescending() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        ArrayList<Document> result = new ArrayList<>();
        result.add(docs[2]);
        result.add(docs[1]);
        result.add(docs[4]);
        assertEquals(result, store.search("tech"));
    }

    // Make sure can search different keywords for different results
    @Test
    public void searchWorksDifKey() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        ArrayList<Document> result = new ArrayList<>();
        result.add(docs[3]);
        result.add(docs[1]);
        assertEquals(result, store.search("fear"));
    }
    // Make sure new words appear in new searches after things are added, and can add to the middle
    @Test
    public void searchWorksAddLater() throws URISyntaxException, IOException {
        DocumentStore store = halfStore();
        Document[] docs = newDocs();
        InputStream[] streams = newStreams();
        URI[] uris = getURIs();
        ArrayList<Document> result = new ArrayList<>();
        result.add(docs[1]);
        assertEquals(result, store.search("fear"));
        store.putDocument(streams[3], uris[3], DocumentFormat.TXT);
        result.add(0, docs[3]); // this should make it before docs[1], since it has more fears
        assertEquals(result, store.search("fear"));
    }

    // Make sure if delete a document, it no longer returns in search
    // Whether deleted regularly or with put(null)
    @Test
    public void searchIgnoresDeletedDocs() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        URI[] uris = getURIs();
        ArrayList<Document> result = new ArrayList<>();
        result.add(docs[2]);
        result.add(docs[1]);
        result.add(docs[4]);
        assertEquals(result, store.search("tech"));
        store.deleteDocument(uris[1]);
        result.remove(docs[1]);
        assertEquals(result, store.search("tech"));
        store.putDocument(null, uris[2], DocumentFormat.TXT);
        result.remove(docs[2]);
        assertEquals(result, store.search("tech"));
    }

    // test can delete and undo and it will return in searches, whether deleteDoc or put(null)
    @Test
    public void searchSeesDeletedDocsUndone() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        URI[] uris = getURIs();
        ArrayList<Document> result = new ArrayList<>();
        result.add(docs[2]);
        result.add(docs[1]);
        result.add(docs[4]);
        assertEquals(result, store.search("tech"));
        store.deleteDocument(uris[1]);
        result.remove(docs[1]);
        assertEquals(result, store.search("tech"));
        store.undo();
        result.add(1, docs[1]);
        assertEquals(result, store.search("tech"));
        store.putDocument(null, uris[2], DocumentFormat.TXT);
        result.remove(docs[2]);
        assertEquals(result, store.search("tech"));
        store.undo(uris[2]);
        result.add(0, docs[2]);
        assertEquals(result, store.search("tech"));
    }

    // Make sure empty list if no matches
    @Test
    public void searchReturnsEmpty() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        ArrayList<Document> result = new ArrayList<>();
        assertEquals(result, store.search("orange"));
    }

    // Make sure empty list if only match is deep in binary
    @Test
    public void searchIgnoresBinary() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        ArrayList<Document> result = new ArrayList<>();
        assertEquals(result, store.search("binary"));
    }

    // Make sure empty list if symbol word, even if matches
    @Test
    public void searchIgnoresSymbols() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        ArrayList<Document> result = new ArrayList<>();
        assertEquals(result, store.search("?"));
    }

    // Tests for searchByPrefix
    // Make sure prefix works, and in descending order
    // Makes sure ignores case and symbols
    @Test
    public void searchByPrefixWorksDescending() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        ArrayList<Document> result = new ArrayList<>();
        result.add(docs[2]);
        result.add(docs[4]);
        result.add(docs[1]);
        result.add(docs[0]);
        assertEquals(result, store.searchByPrefix("tech"));
    }

    // Makes sure new words appear in new searches after things are added, and can add to the middle
    @Test
    public void searchByPrefixWorksAddLater() throws URISyntaxException, IOException {
        DocumentStore store = halfStore();
        Document[] docs = newDocs();
        InputStream[] streams = newStreams();
        URI[] uris = getURIs();
        ArrayList<Document> result = new ArrayList<>();
        result.add(docs[2]);
        result.add(docs[1]);
        result.add(docs[0]);
        assertEquals(result, store.searchByPrefix("tech"));
        store.putDocument(streams[4], uris[4], DocumentFormat.TXT);
        result.add(1, docs[4]);
        assertEquals(result, store.searchByPrefix("tech"));
        assertEquals(result, store.searchByPrefix("tech"));
    }

    // Make sure can search different keywords for different results
    @Test
    public void searchByPrefixDifPrefix() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        ArrayList<Document> result = new ArrayList<>();
        result.add(docs[2]);
        result.add(docs[4]);
        result.add(docs[3]);
        result.add(docs[0]);
        assertTrue(result.containsAll(store.searchByPrefix("I")));
        assertTrue(store.searchByPrefix("I").containsAll(result));
    }

    // Make sure if delete a document, it no longer returns in searchByPrefix
    // Whether deleted regularly or with put(null)
    @Test
    public void searchByPrefixIgnoresDeletedDocs() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        URI[] uris = getURIs();
        ArrayList<Document> result = new ArrayList<>();
        result.add(docs[2]);
        result.add(docs[4]);
        result.add(docs[1]);
        result.add(docs[0]);
        assertEquals(result, store.searchByPrefix("tech"));
        store.deleteDocument(uris[1]);
        result.remove(docs[1]);
        result.remove(docs[2]);
        assertTrue(result.containsAll(store.searchByPrefix("technology")));
        assertTrue(store.searchByPrefix("technology").containsAll(result));
        store.putDocument(null, uris[2], DocumentFormat.TXT);
        result.remove(docs[2]);
        assertEquals(result, store.searchByPrefix("tech"));
    }

    // test can delete and undo and it will return in searches, whether deleteDoc or put(null)
    @Test
    public void searchByPrefixSeesDeletedDocsUndone() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        URI[] uris = getURIs();
        ArrayList<Document> result = new ArrayList<>();
        result.add(docs[2]);
        result.add(docs[4]);
        result.add(docs[1]);
        result.add(docs[0]);
        assertEquals(result, store.searchByPrefix("tech"));
        store.deleteDocument(uris[1]);
        result.remove(docs[1]);
        assertEquals(result, store.searchByPrefix("tech"));
        store.undo();
        result.add(2, docs[1]);
        assertEquals(result, store.searchByPrefix("tech"));
        store.putDocument(null, uris[2], DocumentFormat.TXT);
        result.remove(docs[2]);
        assertEquals(result, store.searchByPrefix("tech"));
        store.undo(uris[2]);
        result.add(0, docs[2]);
        assertEquals(result, store.searchByPrefix("tech"));
    }

    // Make sure empty list if no matches
    // Make sure empty list if only match is in binary
    // Makes sure empty list if symbol word, even if matches
    @Test
    public void searchByPrefixReturnsEmpty() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        ArrayList<Document> result = new ArrayList<>();
        assertEquals(result, store.searchByPrefix("Indianapolis"));
        assertEquals(result, store.searchByPrefix("doesn't"));
        assertEquals(result, store.searchByPrefix("?"));
    }

    // Tests for deleteAll
    // Makes sure deletes all, but not things with prefix
    @Test
    public void deleteAllWorks() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        URI[] uris = getURIs();
        store.deleteAll("tech");
        ArrayList<Document> result = new ArrayList<>();
        assertEquals(result, store.search("tech"));
        result.add(docs[0]);
        assertEquals(result, store.searchByPrefix("tech"));
        assertEquals(docs[3], store.getDocument(uris[3]));
        assertEquals(docs[5], store.getDocument(uris[5]));
        assertNull(store.getDocument(uris[1]));
    }

    // Makes sure returns set correctly
    @Test
    public void deleteAllReturns() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        URI[] uris = getURIs();
        HashSet<URI> result = new HashSet<>();
        result.add(uris[1]);
        result.add(uris[2]);
        result.add(uris[4]);
        assertEquals(result, store.deleteAll("tech"));
    }

    // Makes sure can delete and then add (and get with getDoc and search)
    @Test
    public void deleteAllCanAddAgain() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        InputStream[] streams = newStreams();
        URI[] uris = getURIs();
        store.deleteAll("tech");
        ArrayList<Document> result = new ArrayList<>();
        assertEquals(result, store.search("tech"));
        store.putDocument(streams[1], uris[1], DocumentFormat.TXT);
        result.add(docs[1]);
        assertEquals(result, store.search("tech"));
    }

    // Make sure removed from trie
    @Test
    public void deleteAllRemovesFromTrie() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        store.deleteAll("tech");
        ArrayList<Document> result = new ArrayList<>();
        assertEquals(result, store.search("tech"));
        assertEquals(result, store.search("addictive"));
        result.add(docs[3]);
        assertEquals(result, store.search("fear"));
    }

    // Makes sure empty set if nothing deleted
    // Make sure empty set if only thing that could be deleted is binary
    // Make sure symbol word doesn't delete anything
    @Test
    public void deleteAllReturnsEmpty() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        HashSet<URI> result = new HashSet<>();
        assertEquals(result, store.deleteAll("ichthiosaurus"));
        assertEquals(result, store.deleteAll("doesn't"));
        assertEquals(result, store.deleteAll("?"));
    }

    // Makes sure ignores case and symbols
    @Test
    public void deleteAllIgnoresCaseAndSymbols() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        URI[] uris = getURIs();
        ArrayList<Document> result = new ArrayList<>();
        result.add(docs[4]);
        store.deleteAll("LOVE");
        assertEquals(result, store.search("technology"));
        store.deleteAll("(cares)");
        result.add(docs[1]);
        assertEquals(result, store.searchByPrefix("tech"));
    }

    // Tests for deleteAllWithPrefix
    // Makes sure deletes all
    @Test
    public void deleteAllWithPrefixWorks() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        URI[] uris = getURIs();
        store.deleteAllWithPrefix("tech");
        ArrayList<Document> result = new ArrayList<>();
        assertEquals(result, store.search("tech"));
        assertEquals(result, store.searchByPrefix("tech"));
        assertEquals(docs[3], store.getDocument(uris[3]));
        result.add(docs[3]);
        assertEquals(result, store.search("fear"));
        assertEquals(docs[5], store.getDocument(uris[5]));
        assertNull(store.getDocument(uris[1]));
    }

    // Makes sure returns set correctly
    @Test
    public void deleteAllWithPrefixReturns() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        URI[] uris = getURIs();
        HashSet<URI> result = new HashSet<>();
        result.add(uris[0]);
        result.add(uris[1]);
        result.add(uris[2]);
        result.add(uris[4]);
        assertEquals(result, store.deleteAllWithPrefix("tech"));
    }

    // Makes sure empty set if nothing deleted
    // Make sure empty set if only thing that could be deleted is binary
    // Make sure symbol word doesn't delete anything
    @Test
    public void deleteAllWithPrefixReturnsEmpty() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        HashSet<URI> result = new HashSet<>();
        assertEquals(result, store.deleteAllWithPrefix("ichthio"));
        assertEquals(result, store.deleteAllWithPrefix("doesn't"));
        assertEquals(result, store.deleteAllWithPrefix("?"));
    }

    // Makes sure ignores case and symbols
    @Test
    public void deleteAllWithPrefixIgnoresCaseAndSymbols() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        URI[] uris = getURIs();
        ArrayList<Document> result = new ArrayList<>();
        result.add(docs[4]);
        store.deleteAllWithPrefix("LOVE");
        assertEquals(result, store.search("technology"));
        store.deleteAllWithPrefix("(cares)");
        result.add(docs[1]);
        assertEquals(result, store.searchByPrefix("tech"));
    }

    // Tests for undo with deleteAll
    // Makes sure can deleteAll and undo
    @Test
    public void deleteAllWorksWithUndo() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        store.deleteAll("tech");
        ArrayList<Document> result = new ArrayList<>();
        assertEquals(result, store.search("tech"));
        store.undo();
        result.add(docs[2]);
        result.add(docs[1]);
        result.add(docs[4]);
        assertEquals(result, store.search("tech"));
    }

    // Makes sure can deleteAll, undo(URI), and the others will stay deleted
    @Test
    public void deleteAllWorksWithUndoURI() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        URI[] uris = getURIs();
        store.deleteAll("tech");
        ArrayList<Document> result = new ArrayList<>();
        assertEquals(result, store.search("tech"));
        store.undo(uris[1]);
        result.add(docs[1]);
        assertEquals(result, store.search("tech"));
        store.undo();
        result.add(0, docs[2]);
        result.add(docs[4]);
        assertEquals(result, store.search("tech"));
    }

    // Makes sure can deleteAll, undo(URI) each doc deleted, and next undo will undo what is below this
    @Test
    public void deleteAllUndoURICanGetRidOfCommandSet() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        URI[] uris = getURIs();
        store.deleteDocument(uris[5]);
        assertNull(store.getDocument(uris[5]));
        store.deleteAll("tech");
        ArrayList<Document> result = new ArrayList<>();
        assertEquals(result, store.search("tech"));
        store.undo(uris[1]);
        result.add(docs[1]);
        assertEquals(result, store.search("tech"));
        store.undo(uris[2]);
        result.add(0, docs[2]);
        assertEquals(result, store.search("tech"));
        store.undo(uris[4]);
        result.add(docs[4]);
        assertEquals(result, store.search("tech"));
        store.undo();
        assertEquals(docs[5], store.getDocument(uris[5]));
    }

    // Makes sure that when undo, also adds back for non-keywords
    @Test
    public void deleteAllUndoWorksForNonKeywords() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        store.deleteAll("tech");
        ArrayList<Document> result = new ArrayList<>();
        result.add(docs[3]);
        assertEquals(result, store.search("fear"));
        store.undo();
        result.add(docs[1]);
        assertEquals(result, store.search("fear"));
    }

    // Makes sure that undo also works with deleteAllWithPrefix
    @Test
    public void deleteAllWithPrefixWorksWithUndo() throws URISyntaxException, IOException {
        DocumentStore store = fullStore();
        Document[] docs = newDocs();
        store.deleteAllWithPrefix("tech");
        ArrayList<Document> result = new ArrayList<>();
        assertEquals(result, store.searchByPrefix("tech"));
        store.undo();
        result.add(docs[2]);
        result.add(docs[4]);
        result.add(docs[1]);
        result.add(docs[0]);
        assertEquals(result, store.searchByPrefix("tech"));
    }

}