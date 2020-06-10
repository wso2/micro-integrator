package org.wso2.micro.integrator.initializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.config.mapper.ConfigParser;

import java.lang.reflect.Constructor;
import java.util.Map;

public class MetricReporterLoader extends ClassLoader {

    String metricHandlerParam;
    private static Log log = LogFactory.getLog(MetricReporterLoader.class);

    public MetricReporterLoader() {
       // metricHandlerParam = (String) axisConfig.getParameter(ServiceBusConstants.METRIC_HANDLER).getValue();
    }

    public void classLoader() {
        Map<String, Object> configs = ConfigParser.getParsedConfigs();
        Object exePeriod = configs.get("prometheus_handler.metric_reporter");
        String metricHandler = metricHandlerParam;
        log.info("Loading the class " + exePeriod);

        if (exePeriod != null) {
            invokeClassMethod(exePeriod.toString());
            } else {
                invokeClassMethod("org.wso2.micro.integrator.prometheus.handler.handler.PrometheusReporter");
            }
    }

    public void invokeClassMethod(String classBinName) {
        try {

            // Create a new JavaClassLoader
            ClassLoader classLoader = this.getClass().getClassLoader();

            // Load the target class using its binary name
            Class loadedMyClass = classLoader.loadClass(classBinName);

            System.out.println("Loaded class name: " + loadedMyClass.getName());

            // Create a new instance from the loaded class
            Constructor constructor = loadedMyClass.getConstructor();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
