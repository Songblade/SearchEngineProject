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

    // Unfortunately, the Document.equals() checks if the hashCode is equal, which doesn't really tell me
    // that these are actually the same documents, so I will also check all the other data segments
    private void testDocumentEquality(Document expected, Document docReturn) {
        assertEquals(expected, docReturn);
        assertEquals(expected.getDocumentTxt(), docReturn.getDocumentTxt());
        assertArrayEquals(expected.getDocumentBinaryData(), docReturn.getDocumentBinaryData());
        assertEquals(expected.getKey(), docReturn.getKey());
    }

    // the following tests test putDocument(), though really also getDocument()

    // I want a test that adding 6 documents can still get all 6
    // This will involve both text and binary documents
    @Test
    public void lotsOfDocs() throws URISyntaxException, IOException {
        DocumentStore store = new DocumentStoreImpl();
        byte[][] bytes = new byte[6][1];
        for (int i = 0; i < 6; i++) {
            bytes[i][0] = (byte) i;
        }
        InputStream[] streams = new InputStream[6];
        for (int i = 0; i < 6; i++) {
            streams[i] = new ByteArrayInputStream(bytes[i]);
        }
        URI[] uris = {new URI("http://java.sun.com/index.html"), new URI("http://java.sun.com/outdex.html"),
                new URI("http://java.sun.com/insideoutdex.html"), new URI("http://java.sun.com/outsideindex.html"),
                new URI("http://java.sun.com/rightsideleftdex.html"), new URI("http://java.sun.com/pokedex.html")};

        Document[] docs = {new DocumentImpl(uris[0], new String(bytes[0])), new DocumentImpl(uris[1], bytes[1]), new DocumentImpl(uris[2], new String(bytes[2])),
                new DocumentImpl(uris[3], bytes[3]), new DocumentImpl(uris[4], new String(bytes[4])), new DocumentImpl(uris[5], bytes[5])};
        store.putDocument(streams[0], uris[0], DocumentFormat.TXT);
        store.putDocument(streams[1], uris[1], DocumentFormat.BINARY);
        store.putDocument(streams[2], uris[2], DocumentFormat.TXT);
        store.putDocument(streams[3], uris[3], DocumentFormat.BINARY);
        store.putDocument(streams[4], uris[4], DocumentFormat.TXT);
        store.putDocument(streams[5], uris[5], DocumentFormat.BINARY);
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