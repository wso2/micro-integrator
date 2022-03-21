/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Manages deployed and undeployed artifacts in micro integrator to notify the dashboard.
 */
public class ArtifactDeploymentListener {

    private ArtifactDeploymentListener() {

    }
    private static final Log log = LogFactory.getLog(ArtifactDeploymentListener.class);

    private static JsonArray deployedArtifacts = new JsonArray();
    private static JsonArray undeployedArtifacts = new JsonArray();

    public static void addToDeployedArtifactsQueue(JsonObject deployedArtifact) {
        if (HeartBeatComponent.isDashboardConfigured()) {
            log.debug("Adding " + deployedArtifact.get("type").toString() + " " +
                      deployedArtifact.get("name").toString() + " to deployed artifacts queue.");
            deployedArtifacts.add(deployedArtifact);
        }
    }

    public static void addToUndeployedArtifactsQueue(JsonObject undeployedArtifact) {
        if (HeartBeatComponent.isDashboardConfigured()) {
            log.debug("Adding " + undeployedArtifact.get("type").toString() + " " +
                      undeployedArtifact.get("name").toString() + " to undeployed artifacts queue.");
            undeployedArtifacts.add(undeployedArtifact);
        }
    }

    public static JsonArray getDeployedArtifacts() {
        return deployedArtifacts;
    }

    public static JsonArray getUndeployedArtifacts() {
        return undeployedArtifacts;
    }

    public static void removeFromDeployedArtifactsQueue(int artifactsSize) {
        for (int i = 0; i < artifactsSize; i++) {
            deployedArtifacts.remove(0);
        }
    }

    public static void removeFromUndeployedArtifactsQueue(int artifactsSize) {
        for (int i = 0; i < artifactsSize; i++) {
            undeployedArtifacts.remove(0);
        }
    }

}
