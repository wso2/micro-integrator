/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.integrator.initializer.dashboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages updated artifacts in micro integrator using the management api to notify the dashboard.
 */
public class ArtifactUpdateListener {

    private ArtifactUpdateListener() {

    }

    private static final Log log = LogFactory.getLog(ArtifactUpdateListener.class);

    private static JsonArray stateChangedArtifacts = new JsonArray();
    
    public static void addToUpdatedArtifactsQueue(String artifactType, String name) {
        if (HeartBeatComponent.isDashboardConfigured()) {
            JsonObject updatedArtifact = getUpdatedArtifact(artifactType, name);
            log.debug("Adding " + updatedArtifact.get("type").toString() + " " + updatedArtifact.get("name").toString() +
                     " to state changed artifacts queue.");
            stateChangedArtifacts.add(updatedArtifact);
        }
    }

    public static JsonArray getStateChangedArtifacts() {
        return stateChangedArtifacts;
    }

    public static void removeFromUpdatedArtifactQueue(int artifactsSize) {
        for (int i = 0; i < artifactsSize; i++) {
            stateChangedArtifacts.remove(0);
        }
    }

    private static JsonObject getUpdatedArtifact(String type, String name) {
        JsonObject updatedArtifact = new JsonObject();
        updatedArtifact.addProperty("type", type);
        updatedArtifact.addProperty("name", name);
        return updatedArtifact;
    }
}
