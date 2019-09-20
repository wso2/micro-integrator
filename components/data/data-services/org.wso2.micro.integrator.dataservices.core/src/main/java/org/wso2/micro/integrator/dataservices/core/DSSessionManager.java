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

import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;import org.wso2.micro.integrator.dataservices.core.boxcarring.RequestBox;

/**
 * This class manages sessions within data services.
 */
public class DSSessionManager {

	private static final String DS_SESSION_REQUEST_BOX_NAME = "DS_SESSION_REQUEST_BOX";
	
	private static final String DS_BOX_CARRING_FLAG_NAME = "DS_BOX_CARRING_FLAG";
	
	private static ThreadLocal<Map<String, Object>> threadLocalSession = new ThreadLocal<Map<String,Object>>() {
		protected synchronized Map<String, Object> initialValue() {
            return new HashMap<String, Object>();
        }
	};
		
	/**
	 * Returns an object stored in the session with the given name.
	 */
	private static Object getSessionObject(String name) {
		MessageContext messageContext = MessageContext.getCurrentMessageContext();
		if (messageContext != null) {
			ServiceContext serviceContext = messageContext.getServiceContext();
			if (serviceContext != null) {
				return serviceContext.getProperty(name);
			}			
		} else {
			return threadLocalSession.get().get(name);
		}
		return null;
	}
	
	/**
	 * Save the given object in the session with the given name.
	 */
	private static void setSessionObject(String name, Object obj) {
		MessageContext messageContext = MessageContext.getCurrentMessageContext();
		if (messageContext != null) {
			ServiceContext serviceContext = messageContext.getServiceContext();
			if (serviceContext != null) {
				serviceContext.setProperty(name, obj);
			}			
		} else {
			threadLocalSession.get().put(name, obj);
		}
	}
	
	/**
	 * Initializes boxcarring in the session.
	 */
	private static void initBoxCarObject() {
		RequestBox requestBox = new RequestBox();
		setSessionObject(DS_SESSION_REQUEST_BOX_NAME, requestBox);
	}
	
	/**
	 * Returns the current RequestBox used in boxcarring.
	 */
	public static RequestBox getCurrentRequestBox() {
		RequestBox requestBox = (RequestBox) getSessionObject(DS_SESSION_REQUEST_BOX_NAME);
		if (requestBox == null) {
			initBoxCarObject();
			requestBox = (RequestBox) getSessionObject(DS_SESSION_REQUEST_BOX_NAME);
		}
		return requestBox;
	}
	
	/**
	 * Check if the current session is in the middle of an active boxcarring session.
	 */
	public static boolean isBoxcarring() {
		Boolean boxCarring = (Boolean) getSessionObject(DS_BOX_CARRING_FLAG_NAME);
		if (boxCarring == null) {
			return false;
		}
		return boxCarring;
	}
	
	/**
	 * Set the boxcarring status in the current session.
	 */
	public static void setBoxcarring(boolean boxcarring) {
		setSessionObject(DS_BOX_CARRING_FLAG_NAME, boxcarring);
	}
	
}
