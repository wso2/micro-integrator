/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.micro.integrator.core.resolvers;

import org.wso2.micro.integrator.core.resolvers.Resolver;
import org.wso2.micro.integrator.core.resolvers.ResolverException;

/**
 *  System resolver can be used to resolve environment variables in the synapse config.
 */
public class SystemResolver implements Resolver {

    private String input;

    /**
     * set environment variable which needs to resolved
     **/
    @Override
    public void setVariable(String input) {
        this.input = input;
    }

    /**
     * environment variable is resolved in this function
     * @return resolved value for the environment variable
     */
    @Override
    public String resolve() {
        String envValue = System.getenv(input);
        if (envValue == null) {
            throw new ResolverException("Environment variable could not be found");
        }
        return envValue;
    }
}
