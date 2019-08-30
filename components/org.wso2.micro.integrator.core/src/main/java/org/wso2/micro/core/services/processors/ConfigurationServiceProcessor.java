/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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
package org.wso2.micro.core.services.processors;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ConfigurationServiceProcessor {

    protected ConfigurationContext configCtx;

    protected AxisConfiguration axisConfig;

    protected DeploymentEngine deploymentEngine;

    protected BundleContext bundleContext;

    protected Lock lock = new ReentrantLock();

    public ConfigurationServiceProcessor(AxisConfiguration axisConfig, BundleContext bundleContext){
        this.bundleContext = bundleContext;
        this.axisConfig = axisConfig;
        this.deploymentEngine = (DeploymentEngine) this.axisConfig.getConfigurator();
    }

    public abstract void processConfigurationService(ServiceReference sr, int action) throws AxisFault;
}