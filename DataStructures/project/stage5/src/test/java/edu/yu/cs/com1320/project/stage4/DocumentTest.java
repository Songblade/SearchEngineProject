package edu.yu.cs.com1320.project.stage4;

import edu.yu.cs.com1320.project.stage4.impl.DocumentImpl;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentTest {

    private Document doc1;
    private Document doc2;

    public DocumentTest() throws URISyntaxException {
        doc1 = new DocumentImpl(new URI("www.com"), "This is our amazing text");
        doc2 = new DocumentImpl(new URI("www.2.com"), "This is our amazinger text, see how amazing we are?");
    }

    // tests the set and get use time methods
    // make sure that can set and get back
    @Test
    public void timeMethodsWork() {
        doc1.setLastUseTime(440000023);
        assertEquals(440000023, doc1.getLastUseTime());
    }

    // make sure can set, change, and set again
    @Test
    public void timeMethodsCanChangeLater() {
        doc1.setLastUseTime(440000023);
        assertEquals(440000023, doc1.getLastUseTime());
        doc1.setLastUseTime(-3);
        assertEquals(-3, doc1.getLastUseTime());
    }

    // make sure compareTo works when smaller time
    @Test
    public void compareToWorksSmaller() {
        doc1.setLastUseTime(440000023);
        doc2.setLastUseTime(440000024);
        assertTrue(doc1.compareTo(doc2) < 0);
    }

    // make sure compareTo works when equal time
    @Test
    public void compareToWorksEqual() {
        doc1.setLastUseTime(440000023);
        doc2.setLastUseTime(440000023);
        assertEquals(0, doc1.compareTo(doc2));
    }

    // make sure compareTo works when greater time
    @Test
    public void compareToWorksGreater() {
        doc1.setLastUseTime(440000024);
        doc2.setLastUseTime(440000023);
        assertTrue(doc1.compareTo(doc2) > 0);
    }

    // make sure compareTo works even when greater than int
    @Test
    public void compareToWorksBigNumbers() {
        doc1.setLastUseTime((long) Integer.MAX_VALUE * 3);
        doc2.setLastUseTime((long) Integer.MAX_VALUE * 2);
        assertTrue(doc1.compareTo(doc2) > 0);
        doc1.setLastUseTime((long) Integer.MAX_VALUE * 2);
        doc2.setLastUseTime((long) Integer.MAX_VALUE * 2);
        assertEquals(0, doc1.compareTo(doc2));
        doc1.setLastUseTime((long) Integer.MAX_VALUE * 2);
        doc2.setLastUseTime((long) Integer.MAX_VALUE * 3);
        assertTrue(doc1.compareTo(doc2) < 0);
    }

}
