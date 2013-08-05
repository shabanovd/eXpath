/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2013 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id$
 */
package org.exist.javax.xml.xpath;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.exist.Indexer;
import org.exist.collections.Collection;
import org.exist.collections.CollectionConfigurationManager;
import org.exist.collections.IndexInfo;
import org.exist.dom.DefaultDocumentSet;
import org.exist.dom.DocumentImpl;
import org.exist.dom.DocumentSet;
import org.exist.dom.MutableDocumentSet;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.util.Configuration;
import org.exist.util.ConfigurationHelper;
import org.exist.xmldb.XmldbURI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class SimpleTest {
	
	public static final XmldbURI TEST_COLLECTION_URI = XmldbURI.ROOT_COLLECTION_URI.append("test");
	
	private static String XML = "<data><foo bar=\"hello\" /></data>";
	
	@Test
	public void testName() throws Exception {
		
//		startDB();
		
		Properties props = System.getProperties();
//		props.setProperty(
//			"javax.xml.xpath.XPathFactory:"+NamespaceConstant.OBJECT_MODEL_EXISTDB, 
//			"org.exist.javax.xml.xpath.XPathFactoryImpl"
//		);
		props.setProperty(
			"javax.xml.xpath.XPathFactory:"+XPathFactory.DEFAULT_OBJECT_MODEL_URI, 
			"org.exist.javax.xml.xpath.XPathFactory"
		);
		
        DocumentSet docs = configureAndStore(null, 
            new Resource[] {
                new Resource("test1.xml", XML, null),
            });
        
        XPathFactory factory = XPathFactory.newInstance();//NamespaceConstant.OBJECT_MODEL_EXISTDB);
	    XPath xpath = factory.newXPath();
	    
        DBBroker broker = null;
        try {
            broker = db.authenticate("admin", "");
            
    		String value = xpath.evaluate(
    		        "/data/foo/attribute::bar", 
    		        docs.docsToNodeSet());
    		    
			assertEquals("hello", value);

        } finally {
        	db.release(broker);
        }
        
//        stopDB();
	}
	
    protected static BrokerPool db;
    protected static Collection root;
    protected Boolean savedConfig;

    protected DocumentSet configureAndStore(String configuration, Resource[] resources) {
        DBBroker broker = null;
        TransactionManager transact = null;
        Txn transaction = null;
        MutableDocumentSet docs = new DefaultDocumentSet();
        try {
            broker = db.get(db.getSecurityManager().getSystemSubject());
            assertNotNull(broker);
            transact = db.getTransactionManager();
            assertNotNull(transact);
            transaction = transact.beginTransaction();
            assertNotNull(transaction);

//            MetaData md = MetaData.get();
//            assertNotNull(md);

            if (configuration != null) {
                CollectionConfigurationManager mgr = db.getConfigurationManager();
                mgr.addConfiguration(transaction, broker, root, configuration);
            }
            
            for (Resource resource : resources) {
                IndexInfo info = root.validateXMLResource(transaction, broker, XmldbURI.create(resource.docName), resource.data);
                assertNotNull(info);
    
//                if (docs != null) {
//                    Metas docMD = md.getMetas(info.getDocument());
//                    if (docMD == null) {
//                        docMD = md.addMetas(info.getDocument());
//                    }
//                    assertNotNull(docMD);
//                    
//                    for (Entry<String, String> entry : resource.metas.entrySet()) {
//                        docMD.put(entry.getKey(), entry.getValue());
//                    }
//                }
                
                root.store(transaction, broker, info, resource.data, false);
    
                docs.add(info.getDocument());
            }
            
            transact.commit(transaction);
        } catch (Exception e) {
            if (transact != null)
                transact.abort(transaction);
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            db.release(broker);
        }
        
        return docs;
    }

    @Before
    public void setup() {
        DBBroker broker = null;
        TransactionManager transact = null;
        Txn transaction = null;
        try {
            broker = db.get(db.getSecurityManager().getSystemSubject());
            assertNotNull(broker);
            transact = db.getTransactionManager();
            assertNotNull(transact);
            transaction = transact.beginTransaction();
            assertNotNull(transaction);

            root = broker.getOrCreateCollection(transaction, TEST_COLLECTION_URI);
            assertNotNull(root);
            broker.saveCollection(transaction, root);

            transact.commit(transaction);

            Configuration config = BrokerPool.getInstance().getConfiguration();
            savedConfig = (Boolean) config.getProperty(Indexer.PROPERTY_PRESERVE_WS_MIXED_CONTENT);
            config.setProperty(Indexer.PROPERTY_PRESERVE_WS_MIXED_CONTENT, Boolean.TRUE);
        } catch (Exception e) {
            if (transact != null)
                transact.abort(transaction);
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (db != null)
                db.release(broker);
        }
    }

    @After
    public void cleanup() {
        BrokerPool pool = null;
        DBBroker broker = null;
        TransactionManager transact = null;
        Txn transaction = null;
        try {
            pool = BrokerPool.getInstance();
            assertNotNull(pool);
            broker = pool.get(pool.getSecurityManager().getSystemSubject());
            assertNotNull(broker);
            transact = pool.getTransactionManager();
            assertNotNull(transact);
            transaction = transact.beginTransaction();
            assertNotNull(transaction);

            Collection collConfig = broker.getOrCreateCollection(transaction,
                XmldbURI.create(XmldbURI.CONFIG_COLLECTION + "/db"));
            assertNotNull(collConfig);
            broker.removeCollection(transaction, collConfig);

            if (root != null) {
                assertNotNull(root);
                broker.removeCollection(transaction, root);
            }
            transact.commit(transaction);

            Configuration config = BrokerPool.getInstance().getConfiguration();
            config.setProperty(Indexer.PROPERTY_PRESERVE_WS_MIXED_CONTENT, savedConfig);
        } catch (Exception e) {
            if (transact != null)
                transact.abort(transaction);
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (pool != null) pool.release(broker);
        }
    }

    @BeforeClass
    public static void startDB() {
        try {
            File confFile = ConfigurationHelper.lookup("conf.xml");
            Configuration config = new Configuration(confFile.getAbsolutePath());
            config.setProperty(Indexer.PROPERTY_SUPPRESS_WHITESPACE, "none");
            config.setProperty(Indexer.PRESERVE_WS_MIXED_CONTENT_ATTRIBUTE, Boolean.TRUE);
            BrokerPool.configure(1, 5, config);
            db = BrokerPool.getInstance();
            assertNotNull(db);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void stopDB() {
//        TestUtils.cleanupDB();
        cleanupDB();
        BrokerPool.stopAll(false);
        db = null;
        root = null;
    }
    
    public static void cleanupDB() {
        BrokerPool pool = null;
        DBBroker broker = null;
        TransactionManager transact = null;
        Txn transaction = null;
        try {
            pool = BrokerPool.getInstance();
            assertNotNull(pool);
            broker = pool.get(pool.getSecurityManager().getSystemSubject());
            assertNotNull(broker);
            transact = pool.getTransactionManager();
            assertNotNull(transact);
            transaction = transact.beginTransaction();
            assertNotNull(transaction);

            // Remove all collections below the /db root, except /db/system
            Collection root = broker.getOrCreateCollection(transaction, XmldbURI.ROOT_COLLECTION_URI);
            assertNotNull(root);
            for (Iterator<DocumentImpl> i = root.iterator(broker); i.hasNext(); ) {
                DocumentImpl doc = i.next();
                root.removeXMLResource(transaction, broker, doc.getURI().lastSegment());
            }
            broker.saveCollection(transaction, root);
            for (Iterator<XmldbURI> i = root.collectionIterator(broker); i.hasNext(); ) {
                XmldbURI childName = i.next();
                if (childName.equals("system"))
                    continue;
                Collection childColl = broker.getOrCreateCollection(transaction, XmldbURI.ROOT_COLLECTION_URI.append(childName));
                assertNotNull(childColl);
                broker.removeCollection(transaction, childColl);
            }

            // Remove /db/system/config/db and all collection configurations with it
            Collection config = broker.getOrCreateCollection(transaction,
                XmldbURI.create(XmldbURI.CONFIG_COLLECTION + "/db"));
            assertNotNull(config);
            broker.removeCollection(transaction, config);

            transact.commit(transaction);
        } catch (Exception e) {
        	transact.abort(transaction);
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            if (pool != null) pool.release(broker);
        }
    }
    
    protected class Resource {
        final String docName;
        final String data;
        final Map<String, String> metas;
        
        Resource(String docName, String data, Map<String, String> metas) {
            this.docName = docName;
            this.data = data;
            this.metas = metas;
        }
    }
}
