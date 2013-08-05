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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactoryConfigurationException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;


/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 *
 */
public class XPathFactory extends javax.xml.xpath.XPathFactory {

	@Override
	public boolean isObjectModelSupported(String objectModel) {
		throw new RuntimeException("unsupported");
//		return false;
	}

	@Override
	public void setFeature(String name, boolean value) throws XPathFactoryConfigurationException {
		throw new RuntimeException("unsupported");
	}

	@Override
	public boolean getFeature(String name) throws XPathFactoryConfigurationException {
		throw new RuntimeException("unsupported");
//		return false;
	}

	@Override
	public void setXPathVariableResolver(XPathVariableResolver resolver) {
		throw new RuntimeException("unsupported");
	}

	@Override
	public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
		throw new RuntimeException("unsupported");
	}

	@Override
	public XPath newXPath() {
		return new XPathImpl();
	}
}
