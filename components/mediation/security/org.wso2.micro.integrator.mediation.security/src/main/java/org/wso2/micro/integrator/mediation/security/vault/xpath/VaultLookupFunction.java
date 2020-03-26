/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.mediation.security.vault.xpath;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.function.StringFunction;
import org.wso2.micro.integrator.mediation.security.vault.SecureVaultLookupHandler;
import org.wso2.micro.integrator.mediation.security.vault.SecureVaultLookupHandlerImpl;
import org.wso2.micro.integrator.mediation.security.vault.SecretSrcData;

/**
 * Implements the XPath extension function synapse:vault-lookup(scope,prop-name)
 */
public class VaultLookupFunction implements Function {

	private static final Log log = LogFactory.getLog(VaultLookupFunction.class);
	private static final Log trace = LogFactory.getLog(SynapseConstants.TRACE_LOGGER);

	private static final String NULL_STRING = "";

	/** Synapse Message context */
	private final org.apache.synapse.MessageContext synCtx;

	VaultLookupFunction(org.apache.synapse.MessageContext synCtx) {
		this.synCtx = synCtx;
	}

	/**
	 * Returns the string value of the property which is get from the
	 * corresponding context to the provided scope .
	 * vault-lookup('xxx')
	 * 
	 * @param context
	 *            the context at the point in the expression when the function
	 *            is called
	 * @param args
	 *            arguments of the functions
	 * @return The string value of a property
	 * @throws FunctionCallException
	 */
	@Override
	public Object call(Context context, List args) throws FunctionCallException {

		boolean traceOn = synCtx.getTracingState() == SynapseConstants.TRACING_ON;

		boolean traceOrDebugOn = traceOn || log.isDebugEnabled();

		if (args == null || args.size() == 0) {
			if (traceOrDebugOn) {
				traceOrDebug(traceOn, "vault value for lookup is not specified");
			}
			return NULL_STRING;

		}

		String argOne = StringFunction.evaluate(args.get(0), context.getNavigator());
		SecureVaultLookupHandler mediationSecurity;
		try {
			mediationSecurity = SecureVaultLookupHandlerImpl.getDefaultSecurityService();
			return mediationSecurity.evaluate(argOne, getSecretSourceInfo(args), synCtx);
		} catch (Exception msg) {
			throw new FunctionCallException(msg);
		}

	}

	private void traceOrDebug(boolean traceOn, String msg) {
		if (traceOn) {
			trace.info(msg);
		}
		if (log.isDebugEnabled()) {
			log.debug(msg);
		}
	}

	private SecretSrcData getSecretSourceInfo (List args) {
		// since this vault-lookup function accepts 3 arguments.
	    if (args.size() == 3) {
			String secretType = args.get(1).toString();
			boolean isEncrypted = Boolean.parseBoolean(args.get(2).toString());
			return new SecretSrcData(secretType, isEncrypted);
		}
		return new SecretSrcData();
	}

}
