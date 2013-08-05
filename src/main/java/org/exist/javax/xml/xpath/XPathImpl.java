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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import org.exist.Database;
import org.exist.EXistException;
import org.exist.dom.NodeProxy;
import org.exist.dom.StoredNode;
import org.exist.security.PermissionDeniedException;
import org.exist.security.xacml.AccessContext;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.serializers.Serializer;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQuery;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.Type;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class XPathImpl implements XPath {
	
	public XPathImpl() {
	}

    /* (non-Javadoc)
     * @see javax.xml.xpath.XPath#reset()
     */
    @Override
    public void reset() {
    }

    /* (non-Javadoc)
     * @see javax.xml.xpath.XPath#setXPathVariableResolver(javax.xml.xpath.XPathVariableResolver)
     */
    @Override
    public void setXPathVariableResolver(XPathVariableResolver resolver) {
		throw new RuntimeException("unsupported");
    }

    /* (non-Javadoc)
     * @see javax.xml.xpath.XPath#getXPathVariableResolver()
     */
    @Override
    public XPathVariableResolver getXPathVariableResolver() {
		throw new RuntimeException("unsupported");
//        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.xpath.XPath#setXPathFunctionResolver(javax.xml.xpath.XPathFunctionResolver)
     */
    @Override
    public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
		throw new RuntimeException("unsupported");
    }

    /* (non-Javadoc)
     * @see javax.xml.xpath.XPath#getXPathFunctionResolver()
     */
    @Override
    public XPathFunctionResolver getXPathFunctionResolver() {
		throw new RuntimeException("unsupported");
//        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.xpath.XPath#setNamespaceContext(javax.xml.namespace.NamespaceContext)
     */
    @Override
    public void setNamespaceContext(NamespaceContext nsContext) {
		throw new RuntimeException("unsupported");
    }

    /* (non-Javadoc)
     * @see javax.xml.xpath.XPath#getNamespaceContext()
     */
    @Override
    public NamespaceContext getNamespaceContext() {
		throw new RuntimeException("unsupported");
//        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.xpath.XPath#compile(java.lang.String)
     */
    @Override
    public XPathExpression compile(String expression) throws XPathExpressionException {
		throw new RuntimeException("unsupported");
//        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.xpath.XPath#evaluate(java.lang.String, java.lang.Object, javax.xml.namespace.QName)
     */
    @Override
    public Object evaluate(String expression, Object item, QName returnType) throws XPathExpressionException {
        
        Sequence context = null;

        if (item instanceof Sequence) {
            context = (Sequence) item;
        } else if (item instanceof StoredNode) {
            
            final StoredNode node = (StoredNode)item;
            
            context = new NodeProxy(node.getDocument(), node.getNodeId(), node.getNodeType(), node.getInternalAddress());

        } else if (item != null) {
            throw new XPathExpressionException("unsupported item type '"+item.getClass()+"'");
        }
        
        Database db = null;
        DBBroker broker = null;
        try {
            db = BrokerPool.getInstance();
//            broker = db.get(db.getSecurityManager().getSystemSubject());
            broker = db.getActiveBroker();
            final XQuery xquery = broker.getXQueryService();
            
            return xquery.execute(expression, context, AccessContext.INITIALIZE);
        } catch (final EXistException e) {
            throw new XPathExpressionException(e);
        } catch (final XPathException e) {
            throw new XPathExpressionException(e);
        } catch (final PermissionDeniedException e) {
            throw new XPathExpressionException(e);

        } finally {
//            if (db != null)
//                {db.release(broker);}
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.xpath.XPath#evaluate(java.lang.String, java.lang.Object)
     */
    @Override
    public String evaluate(String expression, Object item) throws XPathExpressionException {
        Sequence context = null;

        if (item instanceof Sequence) {
            context = (Sequence) item;
        } else if (item instanceof StoredNode) {
            
            final StoredNode node = (StoredNode)item;
            
            context = new NodeProxy(node.getDocument(), node.getNodeId(), node.getNodeType(), node.getInternalAddress());

        } else if (item != null) {
            throw new XPathExpressionException("unsupported item type '"+item.getClass()+"'");
        }
        
        Database db = null;
        DBBroker broker = null;
        try {
            db = BrokerPool.getInstance();
//            broker = db.get(db.getSecurityManager().getSystemSubject());
            broker = db.getActiveBroker();
            final XQuery xquery = broker.getXQueryService();
            
            final Sequence normalized = xquery.execute(expression, context, AccessContext.INITIALIZE);
            
            final StringBuilder out = new StringBuilder();

            final Serializer serializer = broker.getSerializer();
            serializer.reset();
            try {
                //serializer.setProperties(outputProperties);
                for (final SequenceIterator i = normalized.iterate(); i.hasNext(); ) {
                    final Item next = i.nextItem();
                    
                    if (Type.subTypeOf(next.getType(), Type.ATTRIBUTE)) {
                        final String val = next.getStringValue();
                        out.append(val);
                    
                    } else if (Type.subTypeOf(next.getType(), Type.NODE)) {
                        final String val = serializer.serialize((NodeValue) next);
                        out.append(val);
                    }
                }
                return out.toString();
            } catch (final SAXNotRecognizedException e) {
                throw new XPathExpressionException(e);
            } catch (final SAXNotSupportedException e) {
                throw new XPathExpressionException(e);
            } catch (final SAXException e) {
                throw new XPathExpressionException(e);
            }
        } catch (final EXistException e) {
            throw new XPathExpressionException(e);
        } catch (final XPathException e) {
            throw new XPathExpressionException(e);
        } catch (final PermissionDeniedException e) {
            throw new XPathExpressionException(e);

        } finally {
//            if (db != null)
//                {db.release(broker);}
        }
    }

    /* (non-Javadoc)
     * @see javax.xml.xpath.XPath#evaluate(java.lang.String, org.xml.sax.InputSource, javax.xml.namespace.QName)
     */
    @Override
    public Object evaluate(String expression, InputSource source, QName returnType) throws XPathExpressionException {
		throw new RuntimeException("unsupported");
//        return null;
    }

    /* (non-Javadoc)
     * @see javax.xml.xpath.XPath#evaluate(java.lang.String, org.xml.sax.InputSource)
     */
    @Override
    public String evaluate(String expression, InputSource source) throws XPathExpressionException {
		throw new RuntimeException("unsupported");
//        return null;
    }
}
