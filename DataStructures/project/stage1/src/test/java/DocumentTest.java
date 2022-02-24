import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import edu.yu.cs.com1320.project.stage1.Document;
import edu.yu.cs.com1320.project.stage1.impl.DocumentImpl;
import java.net.URI;
import java.net.URISyntaxException;

public class DocumentTest {
    // Ignore the constructor
    public DocumentTest() {}

    // I need to make sure that getDocumentTxt returns correctly
    // I must make sure getBinaryData returns null if it is a text document
    // I need to make sure that getKey returns correctly
    @Test
    public void getDocumentTxtReturns() throws URISyntaxException {
        Document doc = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "Tech is cool");
        assertEquals("Tech is cool", doc.getDocumentTxt());
        assertNull(doc.getDocumentBinaryData(), "getDocumentBinaryData should be null");
        assertEquals(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), doc.getKey(), "problem in getKey");
    }

    // I must make sure getText returns null if it is a binary document
    // I need to make sure that getBinaryData returns correctly
    @Test
    public void getDocumentBinaryReturns() throws URISyntaxException {
        URI uriExample = new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html");
        byte[] binary = {(byte) 0};
        byte[] binary2 = {(byte) 0};
        Document doc = new DocumentImpl(uriExample, binary);
        assertNull(doc.getDocumentTxt(), "getDocumentTxt is null");
        assertArrayEquals(binary2, doc.getDocumentBinaryData());
    }
    // I will mostly assume that equals works correctly, because I don't know how to test it correctly, but I
    // must make sure to identical Documents are equal
    @Test
    public void getEqualsTrue() throws URISyntaxException {
        URI uriExample = new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html");
        byte[] binary = {(byte) 0};
        byte[] binary2 = {(byte) 0};
        Document doc1 = new DocumentImpl(uriExample, binary);
        Document doc2 = new DocumentImpl(uriExample, binary2);
        assertTrue(doc1.equals(doc2));
        assertTrue(doc2.equals(doc1));
    }

    // and that two different documents aren't equal, even if URI is
    @Test
    public void getEqualsFalse() throws URISyntaxException {
        URI uriExample = new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html");
        Document doc1 = new DocumentImpl(uriExample, "binary");
        Document doc2 = new DocumentImpl(uriExample, "binary2");
        assertFalse(doc1.equals(doc2));
        assertFalse(doc2.equals(doc1));
    }
    // Or text is equal but URI isn't
    @Test
    public void getEqualsFalseURI() throws URISyntaxException {
        Document doc1 = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "binary");
        Document doc2 = new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide.html"), "binary");
        assertFalse(doc1.equals(doc2));
        assertFalse(doc2.equals(doc1));
    }

    // I must make sure that a null URI throws an IllegalArgumentException in the text constructor
    @Test
    public void nullURIIAEText() {
        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(null, "binary");
        });
    }
    // I must make sure that a null String throws IAE
    @Test
    public void nullStringIAE() {
        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), (String) null);
        });
    }
    // and empty string
    @Test
    public void emptyStringIAE() {
        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), "");
        });
    }
    // Null URI in the binary constructor
    @Test
    public void nullURIIAEBinary() {
        byte[] binary = {(byte) 0};
        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(null, binary);
        });
    }
    // I must make sure null array throws IAE
    @Test
    public void nullArrayIAE() {
        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), (byte[]) null);
        });
    }

    // and empty array
    @Test
    public void emptyArrayIAE() {
        byte[] binary = {};
        assertThrows(IllegalArgumentException.class, ()->{
            new DocumentImpl(new URI("http://java.sun.com/j2se/1.3/docs/guide/index.html"), binary);
        });
    }
}