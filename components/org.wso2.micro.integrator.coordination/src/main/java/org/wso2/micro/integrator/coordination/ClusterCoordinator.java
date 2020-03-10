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

package org.wso2.micro.integrator.coordination;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.coordination.node.NodeDetail;
import org.wso2.micro.integrator.ndatasource.common.DataSourceException;

import java.util.List;
import javax.sql.DataSource;

/**
 * Coordinator class to start running Clustering.
 */
public class ClusterCoordinator {
    private static final Log log = LogFactory.getLog(ClusterCoordinator.class);
    private RDBMSCoordinationStrategy rdbmsCoordinationStrategy;

    public ClusterCoordinator(DataSource coordinationDatasource) throws DataSourceException {

        this.rdbmsCoordinationStrategy = new RDBMSCoordinationStrategy(coordinationDatasource);
    }

    public void startCoordinator() {
        rdbmsCoordinationStrategy.joinGroup();
        List<NodeDetail> nodeDetailList = rdbmsCoordinationStrategy.getAllNodeDetails();
        for (NodeDetail nodeDetail : nodeDetailList) {
            log.info("Node connected: " + nodeDetail.getNodeId());
        }
    }
}
