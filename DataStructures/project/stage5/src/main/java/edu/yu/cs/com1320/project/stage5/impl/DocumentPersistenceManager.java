package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {

    public DocumentPersistenceManager(File baseDir){
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {

    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        return null;
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        return false;
    }
}
