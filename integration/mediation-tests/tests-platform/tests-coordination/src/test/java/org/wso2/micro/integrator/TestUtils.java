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

package org.wso2.micro.integrator;

import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.esb.integration.common.extensions.carbonserver.CarbonTestServerManager;
import org.wso2.esb.integration.common.extensions.db.manager.DatabaseManager;
import org.wso2.esb.integration.common.utils.Utils;
import org.wso2.micro.integrator.ntask.coordination.task.store.connector.TaskQueryHelper;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.xml.xpath.XPathExpressionException;

import static org.wso2.esb.integration.common.utils.Utils.ArtifactType;

public class TestUtils {

    public static final int LOG_READ_TIMEOUT = 180;
    public static final int CLUSTER_DEP_TIMEOUT = 120;
    public static final int CLUSTER_TASK_RESCHEDULE_TIMEOUT = 120;
    public static final int MANAGEMENT_API_PORT = 9154;

    private TestUtils() {
        //
    }

    public static CarbonTestServerManager getNode(int offset) throws XPathExpressionException {

        return getNode(offset, new HashMap<>());
    }

    public static CarbonTestServerManager getNode(int offset, Map<String, String> additionalParams)
            throws XPathExpressionException {

        HashMap<String, String> startupParameters = new HashMap<>();
        startupParameters.put("-DportOffset", String.valueOf(offset));
        startupParameters.put("startupScript", "micro-integrator");
        startupParameters.put("-DgracefulShutdown", "false");
        additionalParams.forEach(startupParameters::put);
        return new CarbonTestServerManager(new AutomationContext(), System.getProperty("carbon.zip"),
                                           startupParameters);
    }

    public static void deployArtifacts(String depDir, ArtifactType type, String... tasks) throws Exception {

        for (int i = 0; i < tasks.length; i++) {
            File task = new File(FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "ESB"
                                         + File.separator + type.getDirName() + File.separator + tasks[i] + ".xml");
            Utils.deploySynapseConfiguration(task, depDir, type);
        }
    }

    public static String msgProcessorPauseLog(String name) {
        return "Task paused: [ESB_TASK][" + name + "]";
    }

    public static String msgProcessorResumeLog(String name) {
        return "Task resumed: [ESB_TASK][" + name + "]";
    }

    public static String deploymentLog(String name) {
        return deploymentLog(name, false);
    }

    public static String deploymentLog(String name, boolean isPaused) {
        return "Task scheduled: [ESB_TASK][" + name + "]" + (isPaused ? "[Paused]" : ".");
    }

    public static boolean isTaskExistInStore(String taskName) throws Exception {

        String query = "SELECT * FROM " + TaskQueryHelper.TABLE_NAME + " WHERE " + TaskQueryHelper.TASK_NAME + " = '"
                + taskName + "'";
        try (Connection conn = DriverManager.getConnection(DatabaseManager.getConnectionUrl(),
                                                           DatabaseManager.getUserName(), DatabaseManager.getPwd());
                PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

}
