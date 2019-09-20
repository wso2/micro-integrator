/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.dataservices.core.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.commons.io.output.NullOutputStream;
import org.wso2.micro.integrator.dataservices.core.DBUtils;import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

/**
 * This class represents the data source for an OMElement,
 * where the generation of data is encapsulated in this object,
 * and it is used in on-demand data retrieval, and effectively
 * streaming data.
 */
public class DSOMDataSource implements OMDataSource {
	
	private static final int XMLREADER_DEFAULT_MESSAGE_SIZE = 1024;

	private DataService dataService;
	
	private String opName;
	
	private Map<String, ParamValue> params;
			
	public DSOMDataSource(DataService dataService, String opName,
			Map<String, ParamValue> params) {
		this.dataService = dataService;
		this.opName = opName;
		this.params = params;
	}
	
	public DataService getDataService() {
		return dataService;
	}

	public String getOpName() {
		return opName;
	}

	public Map<String, ParamValue> getParams() {
		return params;
	}

	/**
	 * This method is called when the current request is a in-only operations,
	 * so a result is not expected.
	 */
	public void executeInOnly() throws XMLStreamException {
		/* in case there is a result, write it to /dev/null */
		XMLStreamWriter xmlWriter = DBUtils.getXMLOutputFactory().createXMLStreamWriter(
				new NullOutputStream());
		this.serialize(xmlWriter);
	}

	public void serialize(OutputStream output, OMOutputFormat format) throws XMLStreamException {
		XMLStreamWriter xmlWriter = DBUtils.getXMLOutputFactory().createXMLStreamWriter(output);
		this.serialize(xmlWriter);
	}
	
	public void serialize(Writer writer, OMOutputFormat format) throws XMLStreamException {
		XMLStreamWriter xmlWriter = DBUtils.getXMLOutputFactory().createXMLStreamWriter(writer);
		this.serialize(xmlWriter);
	}
	
	public void execute(XMLStreamWriter xmlWriter)
			throws XMLStreamException {
		try {
			this.getDataService().invoke(xmlWriter, this.getOpName(), this.getParams());
			/* flush the stream, if there's a result */
			if (xmlWriter != null) {
			    xmlWriter.flush();
			}
		} catch (DataServiceFault e) {
			throw new XMLStreamException(e.getMessage(), e);
		}
	}
	
	public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException {
		this.execute(xmlWriter);
	}

	public XMLStreamReader getReader() throws XMLStreamException {
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream(XMLREADER_DEFAULT_MESSAGE_SIZE);
		XMLStreamWriter xmlWriter = DBUtils.getXMLOutputFactory().createXMLStreamWriter(byteOut);
		this.serialize(xmlWriter);
	    xmlWriter.close();
		ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
		return DBUtils.getXMLInputFactory().createXMLStreamReader(byteIn);
	}
	
}
