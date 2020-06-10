package org.wso2.micro.integrator.prometheus.publisher.util;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.Type;

import java.util.Enumeration;

/**
 * Metric Formatter class to format Prometheus Metrics
 */
public class MetricFormatter {

    /**
     * Return the type of metric
     *
     * @param type Metric Prometheus type
     */
    private String typeString(Type type) {

        switch (type) {
            case GAUGE:
                return "gauge";
            case COUNTER:
                return "counter";
            case SUMMARY:
                return "summary";
            case HISTOGRAM:
                return "histogram";
            default:
                return "untyped";
        }
    }

    /**
     * Return a formatted metric
     *
     * @param metricsList the Prometheus Metric from Prometheus Default Registry
     */
    public StringBuilder formatMetrics(Enumeration<MetricFamilySamples> metricsList) {

        StringBuilder metric = new StringBuilder();
        StringBuilder metricsValue = new StringBuilder();

        while (metricsList.hasMoreElements()) {
            MetricFamilySamples metricFamilySamples = metricsList.nextElement();
            metric.append("# HELP ").append(metricFamilySamples.name).append(' ').append(metricFamilySamples.help).
                    append("\n").append("# TYPE ").
                    append(metricFamilySamples.name).append(' ').append(typeString(metricFamilySamples.type)).
                    append("\n");

            StringBuilder metricData = new StringBuilder();
            for (Collector.MetricFamilySamples.Sample sample : metricFamilySamples.samples) {
                StringBuilder labelNames = new StringBuilder();
                if (!sample.labelNames.isEmpty()) {
                    labelNames.append("{");

                    for (int i = 0; i < sample.labelNames.size(); ++i) {
                        labelNames.append(sample.labelNames.get(i)).append("=\"").append(sample.labelValues.get(i)).
                                append("\"");
                        if (i < sample.labelNames.size() - 1) {
                            labelNames.append(", ");
                        } else {
                            labelNames.append("}");
                        }
                    }
                }

                labelNames.append(' ').append(sample.value);
                if (sample.timestampMs != null) {
                    labelNames.append(' ').append(sample.timestampMs.toString());
                }
                metricData.append(metricFamilySamples.name).append(labelNames).append("\n");
            }
            metric.append(metricData);
        }
        return metricsValue.append(metric);
    }
}