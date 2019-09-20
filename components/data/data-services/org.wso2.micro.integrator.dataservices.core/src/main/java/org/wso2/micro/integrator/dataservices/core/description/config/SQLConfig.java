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
package org.wso2.micro.integrator.dataservices.core.description.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.common.DBConstants.AutoCommit;
import org.wso2.micro.integrator.dataservices.common.DBConstants.RDBMS;
import org.wso2.micro.integrator.dataservices.core.DBUtils;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;
import org.wso2.micro.integrator.dataservices.core.auth.ConfigurationBasedAuthenticator;
import org.wso2.micro.integrator.dataservices.core.auth.DynamicUserAuthenticator;
import org.wso2.micro.integrator.dataservices.core.engine.DataService;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import javax.xml.stream.XMLStreamException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is the base class used for all SQL based (RDBMS) data source configurations.
 */
public abstract class SQLConfig extends Config {
	
	private static final Log log = LogFactory.getLog(
            SQLConfig.class);

	private String validationQuery;
	
	private boolean jdbcBatchUpdateSupport;
	
	private AutoCommit autoCommit;
	
	private DynamicUserAuthenticator primaryDynAuth;
	
	private DynamicUserAuthenticator secondaryDynAuth;
	
	/**
	 * This is used to keep the enlisted XADatasource objects
	 */
	private static ThreadLocal<Set<XAResource>> enlistedXADataSources = new ThreadLocal<Set<XAResource>>() {
		protected Set<XAResource> initialValue() {
			return new HashSet<XAResource>();
		}
	};

	public SQLConfig(DataService dataService, String configId, String type, Map<String, String> properties,
                     boolean odataEnable) throws DataServiceFault {
		super(dataService, configId, type, properties, odataEnable);
		/* set validation query, if exists */
		this.validationQuery = this.getProperty(RDBMS.VALIDATION_QUERY);
		this.processAutoCommitValue();
		this.processDynamicAuth();
	}
	
	private void processDynamicAuth() throws DataServiceFault {
		String dynAuthMapping = this.getProperty(RDBMS.DYNAMIC_USER_AUTH_MAPPING);
		if (!DBUtils.isEmptyString(dynAuthMapping)) {
			OMElement dynUserAuthPropEl;
			try {
				dynUserAuthPropEl = AXIOMUtil.stringToOM(dynAuthMapping);
			} catch (XMLStreamException e) {
				throw new DataServiceFault(e, "Error in reading dynamic user auth mapping configuration: " +
				                           e.getMessage());
			}
			OMElement dynUserAuthConfEl = dynUserAuthPropEl.getFirstElement();
			if (dynUserAuthConfEl == null) {
				throw new DataServiceFault("Invalid dynamic user auth mapping configuration");
            } else if (null != dynUserAuthConfEl.getFirstElement()) {
                this.primaryDynAuth = new ConfigurationBasedAuthenticator(dynUserAuthConfEl.toString());
            }
        }
		String dynAuthClass = this.getProperty(RDBMS.DYNAMIC_USER_AUTH_CLASS);
		if (!DBUtils.isEmptyString(dynAuthClass)) {
			try {
				DynamicUserAuthenticator authObj = (DynamicUserAuthenticator) Class.forName(
						dynAuthClass).newInstance();
				if (this.primaryDynAuth == null) {
					this.primaryDynAuth = authObj;
				} else {
					this.secondaryDynAuth = authObj;
				}
			} catch (Exception e) {
				throw new DataServiceFault(e, "Error in creating dynamic user authenticator: " + e.getMessage());
			}
		}
	}
	
	private void processAutoCommitValue() throws DataServiceFault {
		String autoCommitProp = this.getProperty(RDBMS.AUTO_COMMIT);
		if (!DBUtils.isEmptyString(autoCommitProp)) {
			autoCommitProp = autoCommitProp.trim();
			try {
				boolean acBool = Boolean.parseBoolean(autoCommitProp);
				if (acBool) {
					this.autoCommit = AutoCommit.AUTO_COMMIT_ON;
				} else {
					this.autoCommit = AutoCommit.AUTO_COMMIT_OFF;
				}
			} catch (Exception e) {
				throw new DataServiceFault(e, "Invalid autocommit value in config: " + autoCommitProp +
				                           ", autocommit should be a boolean value");
			}		
		} else {
			this.autoCommit = AutoCommit.DEFAULT;			
		}
	}
	
	public DynamicUserAuthenticator getPrimaryDynAuth() {
		return primaryDynAuth;
	}

	public DynamicUserAuthenticator getSecondaryDynAuth() {
		return secondaryDynAuth;
	}

	public boolean hasJDBCBatchUpdateSupport() {
		return jdbcBatchUpdateSupport;
	}
	
	public AutoCommit getAutoCommit() {
		return autoCommit;
	}
		
	protected void initSQLDataSource() throws SQLException, DataServiceFault {
        Connection conn = (Connection) this.createConnection()[0];
		try {
		    /* check if we have JDBC batch update support */
		    this.jdbcBatchUpdateSupport = conn.getMetaData().supportsBatchUpdates();
		} finally {
		    conn.close();
		}
	}
		
	public abstract DataSource getDataSource() throws DataServiceFault;
	
	public abstract boolean isStatsAvailable() throws DataServiceFault;
	
	public abstract int getActiveConnectionCount() throws DataServiceFault;
	
	public abstract int getIdleConnectionCount() throws DataServiceFault;
		
	public String getValidationQuery() {
		return validationQuery;
	}
	
	public Object[] createConnection() throws SQLException, DataServiceFault {
		return this.createConnection(null, null);
	}

    public Object[] createConnection(String user, String pass) throws SQLException, DataServiceFault {
        if (log.isDebugEnabled()) {
            log.debug("Creating data source connection: ThreadID - " + Thread.currentThread().getId());
        }
        DataSource ds = this.getDataSource();
        Boolean xaConn = false;
        if (ds != null) {
            Connection conn;
            if (user != null) {
                conn = ds.getConnection(user, pass);
            } else {
                conn = ds.getConnection();
            }
            Object tds = this.extractSourceDS(ds);
            boolean[] xaResult = this.isXADataSource(tds);
            xaConn = xaResult[0] | xaResult[1];
            if (xaResult[0] && !xaResult[1]) {
                XAConnection xac = ((XADataSource) tds).getXAConnection();
                if (xac != null) {
                    try {
                        Transaction tx = this.getDataService().getDSSTxManager().
                                getTransactionManager().getTransaction();
                        /* add only if there is a transaction */
                        if (tx != null) {
                            XAResource xaResource = ((XAConnection) conn).getXAResource();
                            if (!isXAResourceEnlisted(xaResource)) {
                                tx.enlistResource(xaResource);
                                addToEnlistedXADataSources(xaResource);
                            }
                        }
                    } catch (IllegalStateException e) {
                        // ignore: can be because we are trying to enlist again
                    } catch (Exception e) {
                        throw new DataServiceFault(e, "Error in getting current transaction: " + e.getMessage());
                    }
                }
            }
            return new Object[] { conn, xaConn };
        } else {
            throw new DataServiceFault("The data source is nonexistent");
        }
    }

    /**
     * Check the given datasource object type for XA support.
     *
     * @param tds Datasource object constructed from the configuraiton
     * @return array of two booleans [0] - IsXADataSource, [1] - IsAtomikosXADataSource
     */
    private boolean[] isXADataSource(Object tds) {
        boolean[] result = new boolean[2];
        result[0] = tds instanceof XADataSource;
        if (tds instanceof AtomikosDataSourceBean) {
            AtomikosDataSourceBean atb = (AtomikosDataSourceBean) tds;
            if (atb.getXaDataSource() != null) {
                result[1] = true;
            }
        }
        return result;
    }

    /**
     * Extract the datasource which is being used to create connections.
     *
     * @param ds the datasource created from the configuration.
     * @return the DataSource to be used for creating connections to be pooled.
     */
    private Object extractSourceDS(DataSource ds) {
        Object tds = ds;
        if (ds instanceof org.apache.tomcat.jdbc.pool.DataSource) {
            org.apache.tomcat.jdbc.pool.DataSource jpDS = (org.apache.tomcat.jdbc.pool.DataSource) ds;
            tds = jpDS.getDataSource();
        }
        return tds;
    }

    @Override
	public boolean isActive() {
		try {
			Connection conn = this.getDataSource().getConnection();
			conn.close();
			return true;
		} catch (Exception e) {
			log.error("Error in checking SQL config availability", e);
			return false;
		}
	}
	
	/**
     * This method adds XAResource object to enlistedXADataSources Threadlocal set
     * @param resource
     */
    private void addToEnlistedXADataSources(XAResource resource) {
    	enlistedXADataSources.get().add(resource);
    }
    
    private boolean isXAResourceEnlisted(XAResource resource) {
    	return enlistedXADataSources.get().contains(resource);
    }

	@Override
	public boolean isResultSetFieldsCaseSensitive() {
		return false;
	}
}
