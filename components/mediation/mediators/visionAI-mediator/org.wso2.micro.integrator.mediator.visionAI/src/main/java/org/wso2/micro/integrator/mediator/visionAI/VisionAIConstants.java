package org.wso2.micro.integrator.mediator.visionAI;

import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;

public class VisionAIConstants {
    /**
     * QName of the cache mediator which will be used by the module.
     */
    public static final QName VISIONAI_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE,
            VisionAIConstants.VISIONAI_LOCAL_NAME);

    /**
     * Local name of the visionAI mediator which will be used by the module.
     */
    public static final String VISIONAI_LOCAL_NAME = "vision";

    /**
     * Following names represent the local names used in QNames in MediatorFactory, Serializer and the UI
     * VisionAIMediator.
     */
    public static final String API_KEY = "api";
    public static final String JSON_FILE_PATH = "json";
}
