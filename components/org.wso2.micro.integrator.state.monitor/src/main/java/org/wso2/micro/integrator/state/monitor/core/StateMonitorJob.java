package org.wso2.micro.integrator.dashboard.state.monitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.wso2.micro.integrator.dashboard.manager.DataManager;
import org.wso2.micro.integrator.dashboard.manager.DataManagerSingleton;
import org.wso2.micro.integrator.dashboard.manager.model.GroupList;
import org.wso2.micro.integrator.dashboard.manager.model.NodeList;
import org.wso2.micro.integrator.dashboard.manager.model.NodeListInner;

/**
 * State monitor schedule task class to run the state monitor task.
 */
public class StateMonitorJob implements Job {
    private static final Logger logger = LogManager.getLogger(StateMonitorJob.class);
    private final DataManager dataManager  = DataManagerSingleton.getDataManager();

    public void execute(JobExecutionContext jExeCtx) throws JobExecutionException {
        GroupList groupList = dataManager.fetchGroups();
        for (String groupId : groupList) {
            NodeList nodeList = dataManager.fetchNodes(groupId);
            for (NodeListInner nodeListInner : nodeList) {
                String accessToken = dataManager.getAccessToken(groupId, nodeListInner.getNodeId());
            }
        }
        logger.info("State Monitor Schedule Task is running " + groupList.size());
        // Get the list of groups by calling MI management API
        // get the list of nodes in each group by calling MI management API
        // Loop
        String accessToken = dataManager.getAccessToken(groupId, nodeId);
    }
}
