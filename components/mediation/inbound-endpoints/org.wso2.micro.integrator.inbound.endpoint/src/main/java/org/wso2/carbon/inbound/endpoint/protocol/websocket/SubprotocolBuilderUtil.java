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

package org.wso2.carbon.inbound.endpoint.protocol.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class SubprotocolBuilderUtil {

    private static String SYNAPSE_SUBPROTOCOL_PREFIX = "synapse(";
    private static String SYNAPSE_SUBPROTOCOL_SUFFIX = ")";
    private static String SYNAPSE_CONTENT_TYPE = "contentType=";
    private static String SUBPROTOCOL_SEPERATOR = ",";

    private static final Log log = LogFactory.getLog(SubprotocolBuilderUtil.class);

    public static String buildSubprotocolString(ArrayList<String> contentType, ArrayList<String> otherSubprotocols) {
        String array = "";
        if (contentType != null && !contentType.isEmpty()) {
            for (String content : contentType) {
                String temp = SYNAPSE_SUBPROTOCOL_PREFIX + SYNAPSE_CONTENT_TYPE + "'" + content + "'"
                        + SYNAPSE_SUBPROTOCOL_SUFFIX;
                array = array.concat(temp).concat(",");
            }
        }

        if (otherSubprotocols != null && !otherSubprotocols.isEmpty()) {
            for (String protocol : otherSubprotocols) {
                array = array.concat(protocol).concat(",");
            }
        }

        return array;
    }

    public static String syanapeSubprotocolToContentType(String subprotocol) {
        Pattern pattern = Pattern.compile(SYNAPSE_SUBPROTOCOL_PREFIX + ".*" + SYNAPSE_SUBPROTOCOL_SUFFIX);
        if (pattern.matcher(subprotocol).matches()) {
            subprotocol = subprotocol.replace(SYNAPSE_SUBPROTOCOL_PREFIX + SYNAPSE_CONTENT_TYPE + "'", "")
                    .replace("'" + SYNAPSE_SUBPROTOCOL_SUFFIX, "");
            subprotocol = subprotocol.trim();
            return subprotocol;
        } else {
            return null;
        }
    }

    public static String extractSynapseSubprotocol(String combinedSubprotocol) {
        String[] subprotocolArray = combinedSubprotocol.split(SUBPROTOCOL_SEPERATOR);
        for (String subprotocol : subprotocolArray) {
            if (subprotocol.contains(InboundWebsocketConstants.SYNAPSE_SUBPROTOCOL_PREFIX)) {
                return subprotocol;
            }
        }
        return null;
    }

    public static String contentTypeToSyanapeSubprotocol(String contentType) {
        return SYNAPSE_SUBPROTOCOL_PREFIX + SYNAPSE_CONTENT_TYPE + "'" + contentType + "'" + SYNAPSE_SUBPROTOCOL_SUFFIX;
    }

    public static ArrayList<AbstractSubprotocolHandler> stringToSubprotocolHandlers(String handlerClasses) {
        ArrayList<AbstractSubprotocolHandler> handlerInstances = new ArrayList<>();
        if (handlerClasses != null) {
            String[] arrayClassesImpl = handlerClasses.split(";");
            for (String classImpl : arrayClassesImpl) {
                try {
                    Class c = Class.forName(classImpl);
                    Constructor cons = c.getConstructor();
                    AbstractSubprotocolHandler handlerInstance = (AbstractSubprotocolHandler) cons.newInstance();
                    handlerInstances.add(handlerInstance);
                } catch (ClassNotFoundException e) {
                    String msg = "Class " + classImpl
                            + " not found. Please check the required class is added to the classpath.";
                    log.error(msg, e);
                    throw new SynapseException(e);
                } catch (NoSuchMethodException e) {
                    String msg = "Required constructor is not implemented.";
                    log.error(msg, e);
                    throw new SynapseException(e);
                } catch (InstantiationException | IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                    String msg = "Couldn't create the class instance.";
                    log.error(msg, e);
                    throw new SynapseException(e);
                }
            }
        }
        return handlerInstances;
    }

}
