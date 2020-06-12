/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.micro.integrator.initializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.config.mapper.ConfigParser;

import java.util.Map;

public class MetricReporterLoader extends ClassLoader {
    private static Log log = LogFactory.getLog(MetricReporterLoader.class);

    public static final String METRIC_REPORTER = "metric_reporter";



    public void classLoader() throws ClassNotFoundException {
        Map<String, Object> configs = ConfigParser.getParsedConfigs();
        Object metricReporterClass = configs.get(ServiceBusConstants.PROMETHEUS_HANDLER + "." + METRIC_REPORTER);
        log.info("Loading the class " + metricReporterClass);

        if (metricReporterClass != null) {
            invokeClassMethod(metricReporterClass.toString());
        } else {
            invokeClassMethod("org.wso2.micro.integrator.prometheus.handler.handler.PrometheusReporter");
        }
    }

    public void invokeClassMethod(String classBinName) throws ClassNotFoundException {
        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            // Load the target class using its name
            classLoader.loadClass(classBinName);
        } catch (ClassNotFoundException ex) {
            log.error(ex);
            throw new ClassNotFoundException("No proper MetricReporter class type is defined");
        }
    }
}
