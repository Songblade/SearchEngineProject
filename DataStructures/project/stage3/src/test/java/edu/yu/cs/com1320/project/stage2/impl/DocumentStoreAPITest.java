package edu.yu.cs.com1320.project.stage2.impl;

import edu.yu.cs.com1320.project.stage3.impl.DocumentStoreImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentStoreAPITest {

    @Test
    public void stage2InterfaceCount() {//tests that the class only implements one interface and its the correct one
        @SuppressWarnings("rawtypes")
        Class[] classes = DocumentStoreImpl.class.getInterfaces();
        assertTrue(classes.length == 1);
        assertTrue(classes[0].getName().equals("edu.yu.cs.com1320.project.stage3.DocumentStore"));
    }

    @Test
    public void stage2MethodCount() {
        Method[] methods = DocumentStoreImpl.class.getDeclaredMethods();
        int publicMethodCount = 0;
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                publicMethodCount++;
            }
        }
        assertTrue(publicMethodCount == 9);
    }

    @Test
    public void stage2UndoExists(){
        try {
            new DocumentStoreImpl().undo();
        } catch (Exception e) {}
    }

    @Test
    public void stage2UndoByURIExists(){
        try {
            new DocumentStoreImpl().undo(new URI("hi"));
        } catch (Exception e) {}
    }
}