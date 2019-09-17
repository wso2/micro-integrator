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

import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * This class is used as a helper class in handling XML writing operations.
 */
public class XMLWriterHelper {

	private String namespace;
		
	public XMLWriterHelper(String namespace) {
		this.namespace = namespace;
	}

	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public void startRowElement(XMLStreamWriter xmlWriter, String name,
                                int resultType, Result result, ExternalParamCollection params)
				throws XMLStreamException {
        if (xmlWriter == null) {
            return;
        }
		switch (resultType) {
		case DBConstants.ResultTypes.XML:
			xmlWriter.writeStartElement(this.getNamespace(), name);
			break;
		case DBConstants.ResultTypes.RDF:
			/* <rdf:Description  rdf:about="http://www.product.fake/cd/S10_1678 "> 
			 * e.g. base uri :- http://www.product.fake/cd/{productCode} */
			try {
				String rdfBaseURI = result.getRDFBaseURI();				
			    String value = DBUtils.evaluateString(rdfBaseURI, params);
			    xmlWriter.writeStartElement(DBConstants.RDF_NAMESPACE, 
			    		DBConstants.DBSFields.RDF_DESCRIPTION);
			    xmlWriter.writeAttribute(DBConstants.RDF_NAMESPACE, 
			    		DBConstants.DBSFields.RDF_ABOUT, value);
			} catch (DataServiceFault e) {
				throw new XMLStreamException("Error in writing start row using RDF", e);
			}
			break;
		}
	}
	
	public void startWrapperElement(XMLStreamWriter xmlWriter, String namespace,
			String name, int resultType) throws XMLStreamException {
        if (xmlWriter == null) {
            return;
        }
		String nsPrefix;
		boolean writeNS;
		switch (resultType) {
		case DBConstants.ResultTypes.XML:
			if (name != null) {
			    /* start result wrapper */
			    xmlWriter.writeStartElement(name);
			    /* write default namespace */
			    nsPrefix = xmlWriter.getNamespaceContext().getPrefix(this.getNamespace());
				writeNS = nsPrefix == null || !"".equals(nsPrefix);				
			    if (writeNS) {
			    	xmlWriter.setDefaultNamespace(namespace);
			        xmlWriter.writeDefaultNamespace(namespace);
			    }
			}
			break;
		case DBConstants.ResultTypes.RDF:
			xmlWriter.setDefaultNamespace(namespace);
			xmlWriter.writeStartElement(DBConstants.DEFAULT_RDF_PREFIX,
					DBConstants.DBSFields.RDF, DBConstants.RDF_NAMESPACE);
			xmlWriter.writeNamespace(DBConstants.DEFAULT_RDF_PREFIX, DBConstants.RDF_NAMESPACE);
			xmlWriter.writeDefaultNamespace(namespace);
			break;
		}
	}
	
	public void endElement(XMLStreamWriter xmlWriter) throws XMLStreamException {
        if (xmlWriter == null) {
            return;
        }
		xmlWriter.writeEndElement();
	}
	
	private void writeElementValue(XMLStreamWriter xmlWriter, ParamValue value)
			throws XMLStreamException {
        if (xmlWriter == null) {
            return;
        }
		if (value.getArrayValue() == null && value.getScalarValue() == null &&
                value.getUdt() == null) {
			xmlWriter.writeNamespace(DBConstants.XSI_PREFIX, DBConstants.XSI_NAMESPACE);
			xmlWriter.writeAttribute(DBConstants.XSI_PREFIX, 
					DBConstants.XSI_NAMESPACE, "nil", "true");
		} else {
            if (value.getValueType() == ParamValue.PARAM_VALUE_ARRAY) {
                for (ParamValue val : value.getArrayValue()) {
		            xmlWriter.writeCharacters(val.toString());
                }
            } else {
                xmlWriter.writeCharacters(value.toString());
            }
		}
	}
	
	public void writeResultElement(XMLStreamWriter xmlWriter, String name, ParamValue value,
			QName xsdType, int categoryType, int resultType, ExternalParamCollection params)
			throws XMLStreamException {
        if (xmlWriter == null) {
            return;
        }
		String nsPrefix;
		boolean writeNS;
		switch (resultType) {
		case DBConstants.ResultTypes.XML:			
			xmlWriter.writeStartElement(name);
			/* write default namespace */
		    nsPrefix = xmlWriter.getNamespaceContext().getPrefix(this.getNamespace());
			writeNS = nsPrefix == null || !"".equals(nsPrefix);				
		    if (writeNS) {
		    	xmlWriter.setDefaultNamespace(namespace);
		        xmlWriter.writeDefaultNamespace(namespace);
		    }
            if (value != null) {
			    this.writeElementValue(xmlWriter, value);
            }
			xmlWriter.writeEndElement();
			break;
		case DBConstants.ResultTypes.RDF:
			switch (categoryType) {
			case DBConstants.DataCategory.VALUE:
				/* <productCode rdf:datatype="http://www.w3.org/2001/XMLSchema#string">S10_1678</productCode> */
				xmlWriter.writeStartElement(this.getNamespace(), name);
				String dataTypeString = xsdType.getNamespaceURI() + "#" + xsdType.getLocalPart();
				xmlWriter.writeAttribute(DBConstants.RDF_NAMESPACE, 
						DBConstants.DBSFields.RDF_DATATYPE, dataTypeString);
				this.writeElementValue(xmlWriter, value);
				xmlWriter.writeEndElement();
				break;
			case DBConstants.DataCategory.REFERENCE:
				/* <productLine rdf:resource="http://productLines.com/Motorcycles/"/> */
				try {
					xmlWriter.writeStartElement(this.getNamespace(), name);
					String evalValue = DBUtils.evaluateString(value.toString(), params);
					xmlWriter.writeAttribute(DBConstants.RDF_NAMESPACE,
							DBConstants.DBSFields.RDF_RESOURCE, evalValue);
					xmlWriter.writeEndElement();
				} catch (DataServiceFault e) {
					throw new XMLStreamException("Error in writing result element using RDF", e);
				}
				break;
			}
		}		
	}
	
	public void addAttribute(XMLStreamWriter xmlWriter, String name,
                             ParamValue value, QName xsdType, int resultType)
			throws XMLStreamException {
        if (xmlWriter == null) {
            return;
        }
		switch (resultType) {
		case DBConstants.ResultTypes.XML:
			if (value != null && value.toString() != null) {
			    xmlWriter.writeAttribute(name, value.toString());
			}
			break;
		}		
	}
	
}
