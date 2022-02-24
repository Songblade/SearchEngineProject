import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import edu.yu.cs.com1320.project.stage1.*;
import edu.yu.cs.com1320.project.stage1.impl.*;
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

    // test put returns 0 when first

    // and then the previous doc hashCode when second

    // test throws IAE if URI is null

    // test throws IAE if InputStream is null

    // test that delete actually deletes the document

    // test that delete returns 0 if nothing there

    // test that delete returns hashCode of deleted document

    // the following tests are for deleteDocument()

    // delete actually deletes the document

    // delete returns false if no document was there

    // delete return true if a document was deleted

}