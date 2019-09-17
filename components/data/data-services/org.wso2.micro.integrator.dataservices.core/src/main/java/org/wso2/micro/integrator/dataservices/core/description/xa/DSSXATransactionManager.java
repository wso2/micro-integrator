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
package org.wso2.micro.integrator.dataservices.core.description.xa;

import javax.transaction.Status;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.core.DataServiceFault;

/** 
 * XA transaction manager for DSS.
 */
public class DSSXATransactionManager {
	
	private static final Log log = LogFactory.getLog(
            DSSXATransactionManager.class);
		
	/* flag to check if 'we' began the transaction or not */
	private ThreadLocal<Boolean> beginTx = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() { 
			return false; 
		}
    };
	
	public TransactionManager transactionManager;
	
	public DSSXATransactionManager(TransactionManager userTx) {
		if (userTx == null) {
			log.warn("TransactionManager is not available");
		}
		this.transactionManager = userTx;
	}
	
	public TransactionManager getTransactionManager() {
		return transactionManager;
	}
	
	public void begin() throws DataServiceFault {
		TransactionManager txManager = getTransactionManager();
		if (txManager == null) {
		    return;
		}
		try {
			if (log.isDebugEnabled()) {
				log.debug("DXXATransactionManager.begin()");
			}
			txManager.begin();
			this.beginTx.set(true);				
		} catch (Exception e) {
			throw new DataServiceFault(e, "Error from transaction manager in "
			        + "begin(): " + e.getMessage());
		}
	}
	
	public void commit() throws DataServiceFault {
		TransactionManager txManager = getTransactionManager();
		if (txManager == null) {
            return;
        }
		try {
			if (log.isDebugEnabled()) {
				log.debug("transactionManager.commit()");
			}
			txManager.commit();			
		} catch (Exception e) {
			throw new DataServiceFault(e,
					"Error from transaction manager when committing: " + e.getMessage());
		} finally {
			this.beginTx.set(false);
		}
	}
	
	public void rollback() {
		TransactionManager txManager = getTransactionManager();
		if (txManager == null) {
            return;
        }
		try {
			txManager.rollback();				
		} catch (Exception e) {
			log.warn("Error from transaction manager when "
			        + "rollbacking: " + e.getMessage(), e);
		} finally {
			this.beginTx.set(false);
		}
	}
	
	public boolean isDTXInitiatedByUS() {
	    return this.beginTx.get();
	}
	
	public boolean isInDTX() {
	    TransactionManager txManager = getTransactionManager();
	    if (txManager == null) {
            return false;
        }
		try {
		    return txManager.getStatus() != Status.STATUS_NO_TRANSACTION;
		} catch (Exception e) {
			log.error("Error at 'hasNoActiveTransaction'", e);
			return false;
		}
	}
		
}
