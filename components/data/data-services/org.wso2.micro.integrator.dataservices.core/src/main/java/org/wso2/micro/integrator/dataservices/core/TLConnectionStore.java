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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a thread local connection repository.
 */
public class TLConnectionStore {
	
	private static final Log log = LogFactory.getLog(TLConnectionStore.class);
	
	private static ThreadLocal<Map<String, DataServiceConnection>> tlCons = new ThreadLocal<Map<String, DataServiceConnection>>() {
		@Override
		protected synchronized Map<String, DataServiceConnection> initialValue() {
			return new HashMap<String, DataServiceConnection>();
		}
	};
	
	private static String generateDataServiceConnectionMapId(String confidId, String user, int queryLevel) {
		String userSuffix;
		if (user != null) {
			userSuffix = " # " + user; 
		} else {
			userSuffix = " # #NULL#";
		}
		return confidId + userSuffix + " # " + queryLevel;
	}
	
	public static void addConnection(String configId, String user, int queryLevel, DataServiceConnection connection) {
		Map<String, DataServiceConnection> conns = tlCons.get();
		conns.put(generateDataServiceConnectionMapId(configId, user, queryLevel), connection);
	}
	
	public static DataServiceConnection getConnection(String configId, String user, int queryLevel) {
		Map<String, DataServiceConnection> conns = tlCons.get();
		return conns.get(generateDataServiceConnectionMapId(configId, user, queryLevel));
	}
	
	public static void commitAll() {
		Map<String, DataServiceConnection> conns = tlCons.get();
		for (DataServiceConnection conn : conns.values()) {
		    try {
		        conn.commit();
                if (log.isDebugEnabled()) {
                    log.debug("Committing connection: " + conn.toString() + ", ThreadID - " +
                              Thread.currentThread().getId());
                }
		    } catch (Exception e) {
                log.warn("Error in committing connection: " + e.getMessage(), e);
            }
		}
	}
	
	public static void commitNonXAConns() {
        Map<String, DataServiceConnection> conns = tlCons.get();
        for (DataServiceConnection conn : conns.values()) {
            if (!conn.isXA()) {
                try {
                    conn.commit();
                    if (log.isDebugEnabled()) {
                        log.debug("Committing non-XA connection: " + conn.toString() + ", ThreadID - " +
                                  Thread.currentThread().getId());
                    }
                } catch (Exception e) {
                    log.warn("Error in committing non-XA connection: " + e.getMessage(), e);
                }
            }
        }
    }
	
	public static void rollbackAll() {
        Map<String, DataServiceConnection> conns = tlCons.get();
        for (DataServiceConnection conn : conns.values()) {
            try {
                conn.rollback();
                if (log.isDebugEnabled()) {
                    log.debug("Rolling back connection: " + conn.toString() + ", ThreadID - " +
                              Thread.currentThread().getId());
                }
            } catch (Exception e) {
                log.warn("Error in rolling back connection: " + e.getMessage(), e);
            }
        }
    }
    
    public static void rollbackNonXAConns() {
        Map<String, DataServiceConnection> conns = tlCons.get();
        for (DataServiceConnection conn : conns.values()) {
            if (!conn.isXA()) {
                try {
                    conn.rollback();
                    if (log.isDebugEnabled()) {
                        log.debug("Rolling back non-XA connection: " + conn.toString() + ", ThreadID - " +
                                  Thread.currentThread().getId());
                    }
                } catch (Exception e) {
                    log.warn("Error in rolling back non-XA connection: " + e.getMessage(), e);
                }
            }
        }
    }
	
	public static void closeAll() {
        Map<String, DataServiceConnection> conns = tlCons.get();
        for (DataServiceConnection conn : conns.values()) {
            try {
                conn.close();
                if (log.isDebugEnabled()) {
                    log.debug("Closing connection: " + conn.toString() + ", ThreadID - " +
                              Thread.currentThread().getId());
                }
            } catch (Exception e) {
                log.error("Error in closing connection: " + e.getMessage(), e);
            }
        }
        conns.clear();
    }
	
}
