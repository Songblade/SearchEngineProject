import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import edu.yu.cs.com1320.project.stage1.*;
import edu.yu.cs.com1320.project.stage1.impl.*;
import edu.yu.cs.com1320.project.stage1.DocumentStore.DocumentFormat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

public class DocumentStoreTest {
    public DocumentStoreTest(){}
    // I will not test for IOException since I have no idea what causes it and I don't think it has anything
    // to do with my methods, but with InputStream
    // I will also not test the underlying HashTable, because that got its own tests

    // the following tests test putDocument(), though really also getDocument()

    // I want a test that adding 6 documents can still get all 6
    // This will involve both text and binary documents
    @Test
    public void lotsOfDocs() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl();
        byte[] byte1 = {(byte) 0};
        byte[] byte2 = {(byte) 1};
        byte[] byte3 = {(byte) 2};
        InputStream[] streams = {new ByteArrayInputStream(byte1), new ByteArrayInputStream(byte2), new ByteArrayInputStream(byte3)};
        URI[] uris = {new URI("http://java.sun.com/index.html"), new URI("http://java.sun.com/ioutdex.html"),
                new URI("http://java.sun.com/insideoutdex.html"), new URI("http://java.sun.com/outsideindex.html"),
                new URI("http://java.sun.com/rightsideleftdex.html"), new URI("http://java.sun.com/pokedex.html")};
        Document[] docs = {new DocumentImpl(uris[0], new String(byte1)), new DocumentImpl(uris[0], byte1), new DocumentImpl(uris[0], new String(byte2)),
                new DocumentImpl(uris[0], byte2), new DocumentImpl(uris[0], new String(byte3)), new DocumentImpl(uris[0], byte3)};
        store.putDocument(streams[0], uris[0], DocumentFormat.TXT);
        store.putDocument(streams[0], uris[1], DocumentFormat.BINARY);
        store.putDocument(streams[1], uris[2], DocumentFormat.TXT);
        store.putDocument(streams[1], uris[3], DocumentFormat.BINARY);
        store.putDocument(streams[2], uris[4], DocumentFormat.TXT);
        store.putDocument(streams[2], uris[5], DocumentFormat.BINARY);
        assertEquals(docs[0], store.getDocument(uris[0]));
        assertEquals(docs[1], store.getDocument(uris[1]));
        assertEquals(docs[2], store.getDocument(uris[2]));
        assertEquals(docs[3], store.getDocument(uris[3]));
        assertEquals(docs[4], store.getDocument(uris[4]));
        assertEquals(docs[5], store.getDocument(uris[5]));
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
        URI uri2 = new URI("http://java.sun.com/outdex.html");
        assertEquals(doc.hashCode(), store.putDocument(stream2, uri2, DocumentFormat.BINARY));
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
}