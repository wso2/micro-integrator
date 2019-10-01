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
package org.wso2.micro.integrator.dataservices.core;

import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDL20ToAxisServiceBuilder;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.schema.CompilerOptions;
import org.apache.axis2.schema.SchemaCompilationException;
import org.apache.axis2.schema.SchemaCompiler;
import org.apache.axis2.util.XMLPrettyPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.core.engine.CallQuery.WithParam;
import org.wso2.micro.integrator.dataservices.core.auth.UserStoreAuthorizationProvider;
import org.wso2.micro.integrator.dataservices.core.description.config.Config;
import org.wso2.micro.integrator.dataservices.core.description.config.RDBMSConfig;
import org.wso2.micro.integrator.dataservices.core.description.operation.Operation;
import org.wso2.micro.integrator.dataservices.core.description.query.SQLQuery;
import org.wso2.micro.integrator.dataservices.core.engine.CallQuery;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;
import org.wso2.micro.integrator.dataservices.core.engine.DataServiceSerializer;
import org.wso2.micro.integrator.dataservices.core.engine.OutputElementGroup;
import org.wso2.micro.integrator.dataservices.core.engine.ParamValue;
import org.wso2.micro.integrator.dataservices.core.engine.QueryParam;
import org.wso2.micro.integrator.dataservices.core.engine.Result;
import org.wso2.micro.integrator.dataservices.core.engine.StaticOutputElement;
import org.wso2.micro.integrator.dataservices.core.validation.Validator;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.wso2.micro.core.util.CarbonUtils.getSecuredTransformerFactory;

/**
 * This class is used to create a data service using a given WSDL (create
 * contract first data services).
 */
public class WSDLToDataService {
	
	private static final Log log = LogFactory.getLog(WSDLToDataService.class);

	private static final String WSDL20_ROOT_ELEMENT = "description";
	
	private WSDLToDataService() {
	}

	/**
	 * Checks if the given data is from a WSDL2.0 document.
	 * @param wsdlContent The WSDL document data
	 * @return true if it is a WSDL2.0 document
	 * @throws DataServiceFault
	 */
	private static boolean isWSDL20(byte[] wsdlContent) throws DataServiceFault {
		try {
			return (AXIOMUtil.stringToOM(
					new String(wsdlContent, DBConstants.DEFAULT_CHAR_SET_TYPE))
					.getLocalName().equals(WSDL20_ROOT_ELEMENT));
		} catch (Exception e) {
			throw new DataServiceFault(e);
		}
	}

	/**
	 * Creates and deploys a contract first data service with the given WSDL
	 * data.
	 * @param axisConfig The current axis configuration
	 * @param wsdlContent The WSDL content
	 * @throws DataServiceFault
	 */
	public static void deployDataService(AxisConfiguration axisConfig,
			byte[] wsdlContent) throws DataServiceFault {
		try {
			AxisService axisService = getAxisServiceFromWSDL(wsdlContent);
			String serviceName = axisService.getName();
			DataService dataService = createDataServiceFromAxisService(axisService);
			String dsContents = DataServiceSerializer.serializeDataService(dataService).toString();
			writeToRepository(axisConfig, serviceName, dsContents);
		} catch (DataServiceFault e) {
			log.error("Error in deploying contract first data service", e); //TODO
			throw e;
		}
	}
	
	/**
	 * Populates and returns an AxisService from a WSDL.
	 * @param wsdlContent The WSDL content
	 * @return AxisService which represents the given WSDL
	 * @throws DataServiceFault
	 */
	private static AxisService getAxisServiceFromWSDL(byte[] wsdlContent) throws DataServiceFault {
		try {
			AxisService axisService;
			ByteArrayInputStream byteIn = new ByteArrayInputStream(wsdlContent);
			if (isWSDL20(wsdlContent)) {
				axisService = new WSDL20ToAxisServiceBuilder(byteIn, null, null).populateService();
			} else { // Must be WSDL11
				axisService = new WSDL11ToAxisServiceBuilder(byteIn, null, null).populateService();
			}
			return axisService;
		} catch (AxisFault e) {
			String message = "Error in getting AxisService from WSDL";
			throw new DataServiceFault(e, message);
		}
	}
	
	/**
	 * Write the data service to the deployment directory.
	 * @param axisConfig The current AxisConfiguration
	 * @param serviceName The name of the service to be deployed
	 * @param dsContents The contents of the data service configuration
	 * @throws DataServiceFault
	 */
	private static void writeToRepository(AxisConfiguration axisConfig,
			String serviceName, String dsContents) throws DataServiceFault {
		try {
			URL repositoryURL = axisConfig.getRepository();
			String dataservicesFile = repositoryURL.getPath() + File.separator
					+ DBDeployer.DEPLOYMENT_FOLDER_NAME + File.separator
					+ serviceName + "." + DBConstants.DBS_FILE_EXTENSION;
			File parentFile = new File(dataservicesFile).getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}
			/*
			    Security Comment :
			    This dataservices File path is trustworthy, file path cannot be access by the user.
			*/
			BufferedWriter writer = new BufferedWriter(new FileWriter(dataservicesFile));
			writer.write(dsContents);
			writer.close();
			XMLPrettyPrinter.prettify(new File(dataservicesFile));
		} catch (IOException e) {
			String message = "Error in writing the contract first data service to the repository";
			throw new DataServiceFault(e, message);
		}
	}
	
	/**
	 * Creates a dummy data source config.
	 * @param dataService The current data service
	 * @param configId The configuration id of the config to be created
	 * @return The newly created dummy config
	 * @throws DataServiceFault
	 */
	private static Config getDummyConfig(DataService dataService,
                                         String configId) throws DataServiceFault {
		Map<String, String> props = new HashMap<String, String>();
		props.put(DBConstants.RDBMS.DRIVER_CLASSNAME, null);
		props.put(DBConstants.RDBMS.URL, null);
		props.put(DBConstants.RDBMS.USERNAME, null);
		props.put(DBConstants.RDBMS.PASSWORD, null);
		RDBMSConfig config = new RDBMSConfig(dataService, configId, props);
		return config;
	}
	
	private static String extractServiceNameFromHeirachicalName(String name) {
		int lastIndex = name.length() - 1;
		int index = name.lastIndexOf('\\');
		if (index != -1 && index < lastIndex) {
			return name.substring(index + 1);
		}
		index = name.lastIndexOf('/');
		if (index != -1 && index < lastIndex) {
			return name.substring(index + 1);
		}
		return name;
	}
	
	/**
	 * Create a DataService from an AxisService.
	 * @param axisService The AxisService used to create the DS
	 * @return The newly created data service
	 * @throws DataServiceFault
	 */
	@SuppressWarnings("unchecked")
	private static DataService createDataServiceFromAxisService(
			AxisService axisService) throws DataServiceFault {
		DataService dataService = new DataService(
				extractServiceNameFromHeirachicalName(axisService.getName()), 
				null, null, null, DBConstants.ServiceStatusValues.INACTIVE, 
				false, false, null);
        /* setting default authorization provider */
        dataService.setAuthorizationProvider(new UserStoreAuthorizationProvider());
		
		/* add dummy config */
		String dummyConfigId = DBConstants.DEFAULT_CONFIG_ID;
		dataService.addConfig(getDummyConfig(dataService, dummyConfigId));
		
		/* compile schema */
		Map<QName, Document> modelMap;
		Map<QName, String> elementMap;
		try {
			CompilerOptions options = new CompilerOptions();
			SchemaCompiler schemaCompiler = new SchemaCompiler(options);
			schemaCompiler.compile(axisService.getSchema());
			modelMap = schemaCompiler.getProcessedModelMap();
			elementMap = schemaCompiler.getProcessedElementMap();
		} catch (SchemaCompilationException e) {
			throw new DataServiceFault(e, "Error in schema compile");
		}
		
		/* add queries/operations */
		AxisOperation axisOperation;
		String operationName;
		String queryId;
		List<QueryParam> queryParams;
		for (Iterator<AxisOperation> axisOperations = axisService.getOperations(); axisOperations.hasNext();) {
			axisOperation = axisOperations.next();
			operationName = axisOperation.getName().getLocalPart(); 
			queryId = operationName + DBConstants.CONTRACT_FIRST_QUERY_SUFFIX;
			queryParams = getQueryParamsFromAxisOperation(modelMap, elementMap, axisOperation);
			/* query */
			dataService.addQuery(new SQLQuery(dataService, queryId, dummyConfigId, false, false, null,
                                              DBConstants.CONTRACT_FIRST_DUMMY_SQL, queryParams,
                                              getResultFromAxisOperation(dataService, axisOperation), null, null,
                                              new HashMap<String, String>(), dataService.getServiceNamespace()));
			/* operation */
			dataService.addOperation(new Operation(dataService, operationName, null,
                                                   getOperationCallQueryFromQueryParams(dataService, queryId, queryParams),
                                                   false, null, false, false));
		}
		return dataService;
	}
	
	private static CallQuery getOperationCallQueryFromQueryParams(DataService dataService, String queryId,
                                                                  List<QueryParam> queryParams) throws
                                                                                                DataServiceFault {
		Map<String, WithParam> withParams = new HashMap<String, WithParam>();
		for (QueryParam qp : queryParams) {
			withParams.put(qp.getName(),
					new WithParam(qp.getName(), qp.getName(), 
					qp.getName(), DBConstants.DBSFields.QUERY_PARAM));
		}
		return new CallQuery(dataService, queryId, withParams, new HashSet<String>());
	}
	
	private static AxisMessage getAxisMessageFromOperation(
			AxisOperation axisOperation, String direction) {
		Iterator<AxisMessage> msgs = axisOperation.getMessages();
		AxisMessage tmpAxisMessage = null;
		while (msgs.hasNext()) {
			tmpAxisMessage = msgs.next();
			if (tmpAxisMessage.getDirection().equals(direction)) {
				return tmpAxisMessage;
			}
		}		
		return null;
	}
	
	/**
	 * Prints the given DOM document,
	 * used for debugging purposes.
	 */
	public static void printDOM(Document dom) {
		try {
			TransformerFactory transformerFactory = getSecuredTransformerFactory();
			DOMSource domSource = new DOMSource(dom);
			StreamResult streamResult = new StreamResult(System.out);
			transformerFactory.newTransformer().transform(domSource, streamResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	private static List<QueryParam> getQueryParamsFromAxisOperation(Map<QName, Document> modelMap,
                                                                    Map<QName, String> elementMap, AxisOperation axisOperation) throws
                                                                                                                                DataServiceFault {
		AxisMessage axisMessage = getAxisMessageFromOperation(axisOperation, "in");
		if (axisMessage == null) {
			throw new DataServiceFault(
					"Valid in message cannot be found for the operation '"
							+ axisOperation.getName() + "'");
		}
		XmlSchemaElement inMsgElement = axisMessage.getSchemaElement();
		/* no query params - return empty list */
		if (inMsgElement == null) {
			return new ArrayList<QueryParam>();
		}
		
		XmlSchemaType inMsgType = inMsgElement.getSchemaType();
		if (!(inMsgType instanceof XmlSchemaComplexType)) {
			throw new DataServiceFault(
					"Xmlschema complex type is expected for the in message of the operation '"
							+ axisOperation.getName() + "'");
		}
			
		QName inMsgTypeName = inMsgElement.getQName();
		String elementName = elementMap.get(inMsgTypeName);
		Document operationDoc = modelMap.get(new QName(inMsgTypeName.getNamespaceURI(), elementName));
		ModelBean operationBean = createModelBean(modelMap, operationDoc);
		List<QueryParam> queryParams = new ArrayList<QueryParam>();
		
		String tmpType;
		List<ModelProperty> props = operationBean.getProperties();
		ModelProperty prop;
		int count = props.size();
		for (int i = 0; i < count; i++) {
			prop = props.get(i);
			tmpType = prop.getSimpleType();
			if (tmpType == null) {
				if (prop.getType().isSimple()) {
					tmpType = prop.getType().getProperties().get(0).getSimpleType();
				} else {
					throw new DataServiceFault(
							"A list of elements with simple types are expected at the in message of the operation '"
							+ axisOperation.getName() + "'");
				}
			}
			queryParams.add(new QueryParam(
                    prop.getName(),
                    DBUtils.getSQLTypeFromXsdType(tmpType),
                    DBConstants.QueryTypes.IN,
                    prop.isArray() ? DBConstants.QueryParamTypes.ARRAY :
						DBConstants.QueryParamTypes.SCALAR,
                    i + 1, // ordinal
                    null, null, new ArrayList<Validator>(), false, false));
		}

		return queryParams;
	}
	
	private static Result getResultFromAxisOperation(DataService dataService, AxisOperation axisOperation)
            throws DataServiceFault {
		AxisMessage axisMessage = getAxisMessageFromOperation(axisOperation, "out");
		// if no out message, then no result
		if (axisMessage == null) {
			return null;
		}		
		String elementName = null, rowName = null, namespace = null;		
		XmlSchemaElement wrapperSchemaElement = axisMessage.getSchemaElement();
		elementName = wrapperSchemaElement.getName();
		namespace = wrapperSchemaElement.getQName().getNamespaceURI();
		
		XmlSchemaType wrapperSchemaType = wrapperSchemaElement.getSchemaType();		
		if (!(wrapperSchemaType instanceof XmlSchemaComplexType)) {
			throw new DataServiceFault(
					"Xmlschema complex type is expected for the out message of the operation '"
							+ axisOperation.getName() + "'");
		}
		XmlSchemaComplexType wrapperSchemaComplexType = (XmlSchemaComplexType) wrapperSchemaType;
		XmlSchemaComplexType dataFieldsType;
		if (hasResultRowName(wrapperSchemaComplexType)) {
			rowName = getResultRowName(wrapperSchemaComplexType);
			dataFieldsType = getRowNameBasedSchemaComplexType(wrapperSchemaComplexType);
		} else {
			dataFieldsType = wrapperSchemaComplexType;
		}
		
		Result result = new Result(elementName, rowName, namespace, null, DBConstants.ResultTypes.XML);
		OutputElementGroup defGroup = new OutputElementGroup(null, null, null, null);
		
		XmlSchemaObjectCollection dataSchemaObjects = getSchemaObjectsFromComplexType(dataFieldsType);
		
		int count = dataSchemaObjects.getCount();		
		XmlSchemaObject sequenceDataObject;
		XmlSchemaElement sequenceDataElement;
		XmlSchemaAttribute sequenceDataAttr;
		for (int i = 0; i < count; i++) {
			sequenceDataObject = dataSchemaObjects.getItem(i);
			if (sequenceDataObject instanceof XmlSchemaElement) {
				sequenceDataElement = (XmlSchemaElement) sequenceDataObject;
				if (!(sequenceDataElement.getSchemaType() instanceof XmlSchemaSimpleType)) {
					throw new DataServiceFault(
							"Xmlschema sequence's data fields at the out message of the operation '"
									+ axisOperation.getName() +
									"' should only contain xml elements with simple types");
				}
				defGroup.addElementEntry(new StaticOutputElement(dataService,
                                                                 sequenceDataElement.getName(), sequenceDataElement
								.getName(), sequenceDataElement.getName(),
                                                                 DBConstants.DBSFields.COLUMN,
                                                                 DBConstants.DBSFields.ELEMENT, namespace,
                                                                 sequenceDataElement.getSchemaTypeName(),
                                                                 new HashSet<String>(), DBConstants.DataCategory.VALUE,
                                                                 DBConstants.ResultTypes.XML, null, ParamValue.PARAM_VALUE_SCALAR, null));
			} else if (sequenceDataObject instanceof XmlSchemaAttribute) {
				sequenceDataAttr = (XmlSchemaAttribute) sequenceDataObject;
				defGroup.addElementEntry(new StaticOutputElement(dataService,
                                                                 sequenceDataAttr.getName(), sequenceDataAttr
								.getName(), sequenceDataAttr.getName(),
                                                                 DBConstants.DBSFields.COLUMN,
                                                                 DBConstants.DBSFields.ATTRIBUTE, namespace,
                                                                 sequenceDataAttr.getSchemaTypeName(),
                                                                 new HashSet<String>(), DBConstants.DataCategory.VALUE,
                                                                 DBConstants.ResultTypes.XML, null, ParamValue.PARAM_VALUE_SCALAR, null));
			} else {
				throw new DataServiceFault(
						"Xmlschema sequence at the out message's data field section of the operation '"
								+ axisOperation.getName()
								+ "' should only contain xml elements/attributes");
			}
		}
		result.setDefaultElementGroup(defGroup);
		return result;
	}
	
	private static boolean hasResultRowName(XmlSchemaComplexType wrapperSchemaComplexType) {
		XmlSchemaParticle wrapperSchemaParticle = wrapperSchemaComplexType.getParticle();
		// a single sequence must be there
		if (!(wrapperSchemaParticle instanceof XmlSchemaSequence)) {
			return false;
		}
		XmlSchemaSequence wrapperSchemaSequence = (XmlSchemaSequence) wrapperSchemaParticle;
		XmlSchemaObjectCollection objects = wrapperSchemaSequence.getItems(); 
		if (objects.getCount() != 1) {
			return false;
		}
		XmlSchemaObject schemaObject = objects.getItem(0); 
		if (!(schemaObject instanceof XmlSchemaElement)) {
			return false;
		}
		XmlSchemaElement schemaElement = (XmlSchemaElement) schemaObject;
		if (!((((XmlSchemaComplexType) schemaElement.getSchemaType())
				.getParticle()) instanceof XmlSchemaSequence)) {
			return false;
		}
		// cannot contain any attributes
		if (wrapperSchemaComplexType.getAttributes().getCount() > 0) {
			return false;
		}
		
		return true;
	}
	
	private static String getResultRowName(XmlSchemaComplexType wrapperSchemaComplexType) {
		return ((XmlSchemaElement) ((XmlSchemaSequence) wrapperSchemaComplexType
				.getParticle()).getItems().getItem(0)).getName();
	}
	
	private static XmlSchemaComplexType getRowNameBasedSchemaComplexType(
			XmlSchemaComplexType wrapperSchemaComplexType) {
		return (((XmlSchemaComplexType) ((XmlSchemaElement) ((XmlSchemaSequence) wrapperSchemaComplexType
				.getParticle()).getItems().getItem(0)).getSchemaType()));
	}
	
	private static XmlSchemaObjectCollection getSchemaObjectsFromComplexType(XmlSchemaComplexType schemaComplexType) {
		XmlSchemaObjectCollection collection = new XmlSchemaObjectCollection();
		XmlSchemaSequence sequence = (XmlSchemaSequence) schemaComplexType.getParticle();
		if (sequence != null) {
			XmlSchemaObjectCollection seqItems = sequence.getItems();
			int c = seqItems.getCount();
			for (int i = 0; i < c; i++) {
				// add elements
				collection.add(seqItems.getItem(i));
			}
		}
		XmlSchemaObjectCollection attrItems = schemaComplexType.getAttributes();
		int c = attrItems.getCount();
		for (int i = 0; i < c; i++) {
			// add attributes
			collection.add(attrItems.getItem(i));
		}
		return collection;
	}
	
	private static ModelBean createModelBean(Map<QName, Document> modelMap, QName typeName) {
		return createModelBean(modelMap, modelMap.get(typeName));
	}
	
	private static ModelBean createModelBean(Map<QName, Document> modelMap, Document doc) {
		ModelBean bean = new ModelBean();
		if (doc == null) {
			return null;
		}
		Node beanEl = doc.getFirstChild();
		/* populate bean attributes */
		NamedNodeMap beanAttrs = beanEl.getAttributes();
		bean.setName(beanAttrs.getNamedItem("originalName").getNodeValue());
		bean.setNsURI(beanAttrs.getNamedItem("nsuri").getNodeValue());
		Node isSimpleNode = beanAttrs.getNamedItem("simple");
		if (isSimpleNode != null) {
			bean.setSimple("yes".equals(isSimpleNode.getNodeValue()));
		} else {
			bean.setSimple(false);
		}
		/* populate child elements / properties */
		NodeList propsElList = beanEl.getChildNodes();
		int count = propsElList.getLength();
		Node propEl;
		for (int i = 0; i < count; i++) {
			propEl = propsElList.item(i);
			bean.addProperty(createModelProperty(modelMap, propEl));
		}
		return bean;
	}
	
	private static ModelProperty createModelProperty(Map<QName, Document> modelMap, Node propEl) {
		ModelProperty property = new ModelProperty();
		NamedNodeMap propAttrs = propEl.getAttributes();
		property.setName(propAttrs.getNamedItem("name").getNodeValue());
		Node isArrayNode = propAttrs.getNamedItem("array");
		if (isArrayNode != null) {
			property.setArray("yes".equals(isArrayNode.getNodeValue()));
		} else {
			property.setArray(false);
		}
		Node isPrimitiveNode = propAttrs.getNamedItem("primitive");
		boolean primitive;
		if (isPrimitiveNode != null) {
			primitive = "yes".equals(isPrimitiveNode.getNodeValue());
		} else {
			primitive = false;
		}
		Node isSimpleNode = propAttrs.getNamedItem("simple");
		boolean simple;
		if (isSimpleNode != null) {
			simple = "yes".equals(isSimpleNode.getNodeValue());
		} else {
			simple = false;
		}
		ModelBean type = null;
		if (!primitive && !simple) {
			type = createModelBean(modelMap,
					new QName(propAttrs.getNamedItem("nsuri").getNodeValue(), 
							propAttrs.getNamedItem("shorttypename").getNodeValue()));
		}
		if (type != null) {
			property.setType(type);
		} else {
			property.setSimpleType(propAttrs.getNamedItem("shorttypename").getNodeValue());
		}
		return property;
	}
	
	/**
	 * Represents a "Bean" element after schema compilation.
	 */
	public static class ModelBean {
	
		private boolean simple;
		
		private String nsURI;
		
		/** type name */
		private String name;
		
		/** list of elements */
		private List<ModelProperty> properties;
		
		public ModelBean() {
			this.properties = new ArrayList<ModelProperty>();
		}

		public boolean isSimple() {
			return simple;
		}

		public void setSimple(boolean simple) {
			this.simple = simple;
		}

		public String getNsURI() {
			return nsURI;
		}

		public void setNsURI(String nsURI) {
			this.nsURI = nsURI;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<ModelProperty> getProperties() {
			return properties;
		}

		public void setProperties(List<ModelProperty> properties) {
			this.properties = properties;
		}
		
		public void addProperty(ModelProperty property) {
			this.getProperties().add(property);
		}
		
		public String toString() {
			StringBuffer buff = new StringBuffer();
			buff.append("{\n");
			buff.append("Name:" + this.getName() + "\n");
			buff.append("NsURI:" + this.getNsURI() + "\n");
			buff.append("Properties: {\n");
			List<ModelProperty> propsList = this.getProperties();
			int count = propsList.size();
			for (int i = 0; i < count; i++) {
				buff.append(propsList.get(i).toString());
				if (i + 1 < count) {
					buff.append(",\n");
				}
			}
			buff.append("}\n");
			buff.append("}\n");
			return buff.toString();
		}
		
	}
	
	/**
	 * Represents a "Property" element after schema compilation.
	 */
	public static class ModelProperty {
				
		/** element name */
		private String name;
				
		private boolean array;
		
		private String simpleType;
		
		private ModelBean type;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isArray() {
			return array;
		}

		public void setArray(boolean array) {
			this.array = array;
		}

		public String getSimpleType() {
			return simpleType;
		}

		public void setSimpleType(String simpleType) {
			this.simpleType = simpleType;
		}

		public ModelBean getType() {
			return type;
		}

		public void setType(ModelBean type) {
			this.type = type;
		}
		
		public String toString() {
			StringBuffer buff = new StringBuffer();
			buff.append("{\n");
			buff.append("Name:" + this.getName() + "\n");
			buff.append("IsArray:" + this.isArray() + "\n");
			if (this.getType() != null) {
			    buff.append("Type:" + this.getType() + "\n");
			} else {
				buff.append("Type:" + this.getSimpleType());
			}
			buff.append("}\n");
			return buff.toString();
		}
		
	}
	
}
