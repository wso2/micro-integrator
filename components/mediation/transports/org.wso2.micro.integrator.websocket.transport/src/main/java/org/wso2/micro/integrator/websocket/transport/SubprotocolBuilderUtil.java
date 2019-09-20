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

package org.wso2.micro.integrator.websocket.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Pattern;

public class SubprotocolBuilderUtil {

    private static String SYNAPSE_SUBPROTOCOL_PREFIX = "synapse(";
    private static String SYNAPSE_SUBPROTOCOL_SUFFIX = ")";
    private static String SYNAPSE_CONTENT_TYPE = "contentType=";

    private static final Log log = LogFactory.getLog(SubprotocolBuilderUtil.class);

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

    public static String contentTypeToSyanapeSubprotocol(String contentType) {
        return SYNAPSE_SUBPROTOCOL_PREFIX + SYNAPSE_CONTENT_TYPE + "'" + contentType + "'" + SYNAPSE_SUBPROTOCOL_SUFFIX;
    }

}
