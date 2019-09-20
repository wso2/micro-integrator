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
package org.wso2.micro.integrator.dataservices.core.description.query;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

import org.apache.axis2.databinding.utils.ConverterUtil;
import org.wso2.micro.integrator.dataservices.common.DBConstants;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;import org.wso2.micro.integrator.dataservices.core.description.event.EventTrigger;import org.wso2.micro.integrator.dataservices.core.engine.DataEntry;import org.wso2.micro.integrator.dataservices.core.engine.DataService;import org.wso2.micro.integrator.dataservices.core.engine.InternalParam;import org.wso2.micro.integrator.dataservices.core.engine.InternalParamCollection;import org.wso2.micro.integrator.dataservices.core.engine.ParamValue;import org.wso2.micro.integrator.dataservices.core.engine.QueryParam;import org.wso2.micro.integrator.dataservices.core.engine.Result;

import javax.xml.stream.XMLStreamWriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class represents a SPARQL data services query.
 */
public abstract class SparqlQueryBase extends Query {

	private String query;

	public SparqlQueryBase(DataService dataService, String queryId,
                           String configId, String query, List<QueryParam> queryParams,
                           Result result, EventTrigger inputEventTrigger,
                           EventTrigger outputEventTrigger,
                           Map<String, String> advancedProperties,
                           String inputNamespace) throws DataServiceFault {
		super(dataService, queryId, queryParams, result, configId,
				inputEventTrigger, outputEventTrigger, advancedProperties,
				inputNamespace);
		this.query = query;
	}

	@Override
	public Object runPreQuery(InternalParamCollection params, int queryLevel)
			throws DataServiceFault {
		return this.processPreQuery(params, queryLevel);
	}

    @Override
    public void runPostQuery(Object result, XMLStreamWriter xmlWriter,
                              InternalParamCollection params, int queryLevel)
            throws DataServiceFault {
        this.processPostQuery(result, xmlWriter, params, queryLevel);
    }

	public RDFNode convertTypeLiteral(Model model, InternalParam param)
			throws DataServiceFault {
		String value = param.getValue().getScalarValue();
		String type = param.getSqlType();
		if (type == null) {
			return model.createTypedLiteral(value);
		} else if (DBConstants.XSDTypes.INTEGER.equals(type) ) {
			return model.createTypedLiteral(value,XSDDatatype.XSDinteger);
		} else if (DBConstants.XSDTypes.ANYURI.equals(type)) {
			return model.createResource(value);
		} else if (DBConstants.XSDTypes.DECIMAL.equals(type)) {
			return model.createTypedLiteral(value,XSDDatatype.XSDdecimal);
		} else if (DBConstants.XSDTypes.DOUBLE.equals(type)) {
			return model.createTypedLiteral(value,XSDDatatype.XSDdouble);
		} else if (DBConstants.XSDTypes.FLOAT.equals(type)) {
			return model.createTypedLiteral(value,XSDDatatype.XSDfloat);
		} else if (DBConstants.XSDTypes.STRING.equals(type)) {
			return model.createTypedLiteral(value);
		} else if (DBConstants.XSDTypes.BOOLEAN.equals(type)) {
			return model.createTypedLiteral(value,XSDDatatype.XSDboolean);
		} else if (DBConstants.XSDTypes.LONG.equals(type)) {
			return model.createTypedLiteral(value,XSDDatatype.XSDlong);
		} else if (DBConstants.XSDTypes.DATE.equals(type) ) {
			return model.createTypedLiteral(value,XSDDatatype.XSDdate);
		} else if (DBConstants.XSDTypes.DATETIME.equals(type) ) {
			return model.createTypedLiteral(value,XSDDatatype.XSDdateTime);
		} else if (DBConstants.XSDTypes.TIME.equals(type)) {
			return model.createTypedLiteral(value,XSDDatatype.XSDtime);
		} else if (DBConstants.XSDTypes.GYEARMONTH.equals(type) ) {
			return model.createTypedLiteral(value,XSDDatatype.XSDgYearMonth);
		} else if (DBConstants.XSDTypes.GYEAR.equals(type) ) {
			return model.createTypedLiteral(value,XSDDatatype.XSDgYear);
		} else if (DBConstants.XSDTypes.GMONTHDAY.equals(type)) {
			return model.createTypedLiteral(value,XSDDatatype.XSDgMonthDay);
		} else if (DBConstants.XSDTypes.GDAY.equals(type)) {
			return model.createTypedLiteral(value,XSDDatatype.XSDgDay);
		} else if (DBConstants.XSDTypes.GMONTH.equals(type)) {
			return model.createTypedLiteral(value,XSDDatatype.XSDgMonth);
		} else if (DBConstants.XSDTypes.HEXBINARY.equals(type)) {
			return model.createTypedLiteral(value,XSDDatatype.XSDhexBinary);
		} else if (DBConstants.XSDTypes.BASE64BINARY.equals(type)) {
			return model.createTypedLiteral(value,XSDDatatype.XSDbase64Binary);
		} else if (DBConstants.XSDTypes.QNAME.equals(type)) {
			return model.createTypedLiteral(value,XSDDatatype.XSDQName);
		} else if (DBConstants.XSDTypes.NOTATION.equals(type)) {
			return model.createTypedLiteral(value,XSDDatatype.XSDNOTATION);
		} else {
			throw new DataServiceFault("[" + this.getDataService().getName()
					+ "]  Found Unsupported data type : " + type
					+ " as input parameter.");
		}

	}
 
	private String convertRSToString (QuerySolution soln, String colName) {
		if (soln.getLiteral(colName).getDatatype() == null) {
			return soln.getLiteral(colName).getString();
		} else {
			String colType = soln.getLiteral(colName).getDatatype().getURI();
			if (colType.equals(XSDDatatype.XSDdecimal.getURI())) {
				return ConverterUtil.convertToString(ConverterUtil.convertToDecimal(soln
								.getLiteral(colName).getString()));
			} else if (colType.equals(XSDDatatype.XSDdouble.getURI())) {
				return ConverterUtil.convertToString(ConverterUtil
						.convertToDouble(soln.getLiteral(colName).getString()));
			} else if (colType.equals(XSDDatatype.XSDfloat.getURI())) {
				return ConverterUtil.convertToString(ConverterUtil
						.convertToFloat(soln.getLiteral(colName).getString()));
			} else if (colType.equals(XSDDatatype.XSDstring.getURI())) {
				return soln.getLiteral(colName).getString();
			} else if (colType.equals(XSDDatatype.XSDboolean.getURI())) {
				return ConverterUtil
						.convertToString(ConverterUtil.convertToBoolean(soln
								.getLiteral(colName).getString()));
			} else if (colType.equals(XSDDatatype.XSDlong.getURI())) {
				return ConverterUtil.convertToString(ConverterUtil
						.convertToLong(soln.getLiteral(colName).getString()));
			} else if (colType.equals(XSDDatatype.XSDdate.getURI())) {
				return ConverterUtil.convertToString(ConverterUtil
						.convertToDate(soln.getLiteral(colName).getString()));
			} else if (colType.equals(XSDDatatype.XSDdateTime.getURI())) {
				return ConverterUtil
						.convertToString(ConverterUtil.convertToDateTime(soln
								.getLiteral(colName).getString()));
			} else if (colType.equals(XSDDatatype.XSDtime.getURI())) {
				return ConverterUtil.convertToString(ConverterUtil
						.convertToTime(soln.getLiteral(colName).getString()));
			} else {
				return soln.getLiteral(colName).getString();
			}
		}	
	}
	
	public DataEntry getDataEntryFromRS(ResultSet rs) {
		DataEntry dataEntry = new DataEntry();
		QuerySolution soln = rs.nextSolution();
		String colName, value;
		boolean useColumnNumbers = this.isUsingColumnNumbers();
		/* for each column get the colName and colValue and add to the data entry */
		for (int i = 0; i < rs.getResultVars().size(); i++) {
			colName = rs.getResultVars().get(i);
			RDFNode node = soln.get(colName) ;  			
			if (node.isLiteral()) {
				value = convertRSToString(soln, colName);
			} else {
				value = soln.getResource(colName).getURI();
			}			
			dataEntry.addValue(useColumnNumbers ? Integer.toString(i + 1) : 
				colName, new ParamValue(value));
		}
		return dataEntry;
	}
	
	public abstract Object processPreQuery(InternalParamCollection params, int queryLevel) throws DataServiceFault ;

    public abstract void processPostQuery(Object result, XMLStreamWriter xmlWriter,
                                           InternalParamCollection params, int queryLevel) throws DataServiceFault ;
	/**
	 * Gets the Query string
	 * @return  String
	 */
	public String getQuery() {
		return query;
	}
	
	/**
	 * Gets a Query Execution for the query
	 * @return QueryExecution
	 * @throws DataServiceFault
	 * @throws IOException 
	 */
	public abstract QueryExecution getQueryExecution() throws IOException, DataServiceFault;
	
	/**
	 * Gets the model used to validate input parameters
	 * @return Model
	 */
	public Model getModelForValidation()
	{
		return ModelFactory.createDefaultModel();
	}
}
