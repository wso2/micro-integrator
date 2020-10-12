/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.mediation.security.vault.external.hashicorp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.function.StringFunction;
import org.wso2.micro.integrator.mediation.security.vault.external.ExternalVaultLookupHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the XPath extension function hashicorp:vault-lookup(alias).
 */
public class HashiCorpVaultLookupFunction implements Function {

    private static final Log log = LogFactory.getLog(HashiCorpVaultLookupFunction.class);
    private static final Log trace = LogFactory.getLog(SynapseConstants.TRACE_LOGGER);

    private static final String NULL_STRING = "";

    /** Synapse Message context */
    private final org.apache.synapse.MessageContext synCtx;

    public HashiCorpVaultLookupFunction(org.apache.synapse.MessageContext synCtx) {
        this.synCtx = synCtx;
    }

    /**
     * Returns the string value of the property which is get from the
     * corresponding context to the provided scope.
     * vault-lookup('xxx')
     *
     * @param context the context at the point in the expression when the function is called
     * @param args arguments of the functions
     * @return The string value of a property
     * @throws FunctionCallException throws FunctionCallException when error occurs
     */
    @Override
    public Object call(Context context, List args) throws FunctionCallException {

        boolean traceOn = synCtx.getTracingState() == SynapseConstants.TRACING_ON;
        boolean traceOrDebugOn = traceOn || log.isDebugEnabled();

        if (args == null || args.isEmpty()) {
            if (traceOrDebugOn) {
                traceOrDebug(traceOn, "vault alias for lookup is not specified");
            }
            return NULL_STRING;
        }

        if (args.size() != 2 && args.size() != 3) {
            if (traceOrDebugOn) {
                traceOrDebug(traceOn, "vault path and field values for lookup is not specified");
            }
            return NULL_STRING;
        }

        // check the args list length and add to the map with keys
        Map<String, String> vaultParameterMap = new HashMap<>();
        if (args.size() == 3) {
            vaultParameterMap.put(HashiCorpVaultConstant.VAULT_NAMESPACE_PARAMETER,
                    StringFunction.evaluate(args.get(0), context.getNavigator()));
            vaultParameterMap.put(HashiCorpVaultConstant.PATH_PARAMETER,
                    StringFunction.evaluate(args.get(1), context.getNavigator()));
            vaultParameterMap.put(HashiCorpVaultConstant.FIELD_PARAMETER,
                    StringFunction.evaluate(args.get(2), context.getNavigator()));
        } else {
            vaultParameterMap.put(HashiCorpVaultConstant.PATH_PARAMETER,
                    StringFunction.evaluate(args.get(0), context.getNavigator()));
            vaultParameterMap.put(HashiCorpVaultConstant.FIELD_PARAMETER,
                    StringFunction.evaluate(args.get(1), context.getNavigator()));
        }

        try {
            ExternalVaultLookupHandler mediationSecurity = HashiCorpVaultLookupHandlerImpl.getDefaultSecurityService();
            return mediationSecurity.evaluate(vaultParameterMap, synCtx);
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
}
