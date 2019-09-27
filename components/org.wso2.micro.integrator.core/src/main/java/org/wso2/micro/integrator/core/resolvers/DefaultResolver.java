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

/**
 * Default Resolver is the class used in general case where any resolver is not used
 */
public class DefaultResolver implements Resolver {

    private String input;

    /**
     * Return the variable passed
     * @return input
     */
    @Override
    public String resolve() {

        return input;
    }

    /**
     * sets the input value
     */
    @Override
    public void setVariable(String input) {
        this.input = input;
    }
}
