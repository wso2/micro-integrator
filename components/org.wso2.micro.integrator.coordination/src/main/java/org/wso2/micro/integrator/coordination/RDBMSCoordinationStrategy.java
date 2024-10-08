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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.config.mapper.ConfigParser;
import org.wso2.micro.core.util.StringUtils;
import org.wso2.micro.integrator.coordination.exception.ClusterCoordinationException;
import org.wso2.micro.integrator.coordination.node.NodeDetail;
import org.wso2.micro.integrator.coordination.util.MemberEventType;
import org.wso2.micro.integrator.coordination.util.RDBMSConstantUtils;
import org.wso2.micro.integrator.coordination.util.StringUtil;
import org.wso2.micro.integrator.ndatasource.common.DataSourceException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.sql.DataSource;

import static org.wso2.micro.integrator.coordination.util.RDBMSConstantUtils.CLUSTER_CONFIG;
import static org.wso2.micro.integrator.coordination.util.RDBMSConstantUtils.NODE_ID;
import static org.wso2.micro.integrator.coordination.util.RDBMSConstantUtils.NODE_ID_CONFIG_NAME;
import static org.wso2.micro.integrator.coordination.util.RDBMSConstantUtils.NODE_ID_SYSTEM_PROPERTY;

/**
 * This class controls the overall process of RDBMS coordination.
 */
public class RDBMSCoordinationStrategy implements CoordinationStrategy {
    /**
     * Class log.
     */
    private static Log log = LogFactory.getLog(RDBMSCoordinationStrategy.class);

    /**
     * Heartbeat interval in milliseconds.
     */
    private int heartBeatInterval;
    /**
     * After this much of time the node is assumed to have left the cluster.
     */
    private int heartbeatMaxRetryInterval;
    /**
     * This is used to give the user a warning when the heartbeat interval exceeds the limit.
     */
    private double heartbeatWarningMargin;

    /**
     * Maximum time taken to read from the database.
     */
    private long maxDBReadTime;

    /**
     * Heartbeat retry interval.
     */
    private int heartbeatMaxRetry;

    /**
     * Interval after which a node is considered inactive after being unresponsive.
     */
    private long inactiveIntervalAfterUnresponsive;

    /**
     * Thread executor used to run the coordination algorithm.
     */
    private final ExecutorService threadExecutor;

    /**
     * Used to send and receive cluster notifications.
     */
    private RDBMSMemberEventProcessor rdbmsMemberEventProcessor;

    /**
     * Identifier used to identify the node uniquely in the cluster
     */
    private String localNodeId;

    /**
     * Identifier used to identify the cluster group
     */
    private String localGroupId;

    private boolean isCoordinatorTasksRunning;


    /**
     * Possible node states
     * <p>
     * +-----------+            +-------------+
     * |   MEMBER  +<---------->+ Coordinator |
     * +-----------+            +-------------+
     */
    private enum NodeState {
        COORDINATOR, MEMBER
    }

    /**
     * Used to communicate with the communication bus context.
     */
    private RDBMSCommunicationBusContextImpl communicationBusContext;

    private RDBMSCoordinationStrategy(RDBMSCommunicationBusContextImpl communicationBusContext) {
        readCoordinationConfigurations();
        if (heartbeatMaxRetry < 1) {
            throw new ClusterCoordinationException("heartbeatMaxRetry configuration should be larger than 0");
        }
        this.heartbeatMaxRetryInterval = heartBeatInterval * heartbeatMaxRetry;
        this.heartbeatWarningMargin = heartbeatMaxRetryInterval * 0.75;
        // maxDBReadTime is set to 1/10th of the heartbeatWarningMargin as for the max possible number of db calls
        this.maxDBReadTime = (long) (heartbeatWarningMargin / 10);
        this.inactiveIntervalAfterUnresponsive = heartbeatMaxRetryInterval * 2L;

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setPriority(7)
                                                                     .setNameFormat("RDBMSCoordinationStrategy-%d").build();
        this.threadExecutor = Executors.newSingleThreadExecutor(namedThreadFactory);

        this.localNodeId = getNodeId();
        this.communicationBusContext = communicationBusContext;
        this.rdbmsMemberEventProcessor = new RDBMSMemberEventProcessor(localNodeId, localGroupId,
                (int) heartbeatWarningMargin, communicationBusContext, maxDBReadTime);
    }

    /**
     * Instantiate RDBMSCoordinationStrategy with provided Datasource
     */
    public RDBMSCoordinationStrategy(DataSource dataSource) throws DataSourceException {


        this(new RDBMSCommunicationBusContextImpl(dataSource));
    }

    private void readCoordinationConfigurations() {
        String heartBeatInterval = System.getProperty(RDBMSConstantUtils.HEART_BEAT_INTERVAL);
        if (heartBeatInterval == null) {
            heartBeatInterval = System.getenv(RDBMSConstantUtils.HEART_BEAT_INTERVAL);
        }
        if (heartBeatInterval == null){
            this.heartBeatInterval = RDBMSConstantUtils.DEFAULT_HEART_BEAT_INTERVAL;
        } else {
            try{
                this.heartBeatInterval = Integer.parseInt(heartBeatInterval);
            } catch (NumberFormatException e){
                this.heartBeatInterval = RDBMSConstantUtils.DEFAULT_HEART_BEAT_INTERVAL;
            }
        }

        String heartBeatMaxRetry = System.getProperty(RDBMSConstantUtils.HEART_BEAT_MAX_RETRY);
        if (heartBeatMaxRetry == null) {
            heartBeatMaxRetry = System.getenv(RDBMSConstantUtils.HEART_BEAT_MAX_RETRY);
        }
        if (heartBeatMaxRetry == null){
            this.heartbeatMaxRetry = RDBMSConstantUtils.DEFAULT_HEART_BEAT_MAX_RETRY;
        } else {
            try{
                this.heartbeatMaxRetry = Integer.parseInt(heartBeatMaxRetry);
            } catch (NumberFormatException e){
                this.heartbeatMaxRetry = RDBMSConstantUtils.DEFAULT_HEART_BEAT_MAX_RETRY;
            }
        }

        String localGourpId = System.getProperty(RDBMSConstantUtils.LOCAL_GROUP_ID);
        if (localGourpId == null || localGourpId.length() == 0) {
            localGourpId = System.getenv(RDBMSConstantUtils.LOCAL_GROUP_ID);
        }
        if (localGourpId == null || localGourpId.length() == 0) {
            localGourpId = RDBMSConstantUtils.DEFAULT_LOCAL_GROUP_ID;
        }
        this.localGroupId = localGourpId;
    }

    @Override
    public List<NodeDetail> getAllNodeDetails() throws ClusterCoordinationException {
        List<NodeDetail> allNodeDetails = communicationBusContext.getAllNodeData(localGroupId);
        List<NodeDetail> liveNodeDetails = new ArrayList<>();
        for (NodeDetail nodeDetail : allNodeDetails) {
            long heartbeatAge = System.currentTimeMillis() - nodeDetail.getLastHeartbeat();
            if (heartbeatAge < heartbeatMaxRetryInterval) {
                liveNodeDetails.add(nodeDetail);
            }
        }
        return liveNodeDetails;
    }

    @Override
    public NodeDetail getLeaderNode() {
        List<NodeDetail> nodeDetails = communicationBusContext.getAllNodeData(localGroupId);
        for (NodeDetail nodeDetail : nodeDetails) {
            if (nodeDetail.isCoordinator()) {
                return nodeDetail;
            }
        }
        return null;
    }

    @Override
    public boolean isLeaderNode() throws ClusterCoordinationException {
        NodeDetail nodeDetail = communicationBusContext.getNodeData(localNodeId, localGroupId);
        if (nodeDetail == null) {
            return false;
        }
        return nodeDetail.isCoordinator();
    }

    /**
     * Checks whether a node with same id already exists in cluster.
     *
     * @return whether this is duplicate node or not.
     */
    boolean isDuplicatedNode() {

        boolean isNodeExist = false;
        NodeDetail nodeDetail = communicationBusContext.getNodeData(localNodeId, localGroupId);
        if (nodeDetail != null) {
            /*This check is done to verify if the node details in the database are of an inactive node.
            This check would fail if a node goes down and is restarted before the heart beat value expires.*/
            long heartbeatAge = System.currentTimeMillis() - nodeDetail.getLastHeartbeat();
            isNodeExist = (heartbeatAge < heartbeatMaxRetryInterval);
        }
        return isNodeExist;
    }

    int getHeartbeatMaxRetryInterval() {
        return heartbeatMaxRetryInterval;
    }

    @Override
    public void joinGroup() {
        boolean retryClusterJoin = false;
        long inactivityTime = 0L;
        do {
            //todo move inactive time to toml
            if (System.currentTimeMillis() - inactivityTime >= 5000) {
                try {
                    //clear old membership events for the node
                    communicationBusContext.clearMembershipEvents(localNodeId, localGroupId);
                    isCoordinatorTasksRunning = true;
                    retryClusterJoin = false;
                    this.threadExecutor.execute(new HeartBeatExecutionTask(false));
                    log.info("Successfully joined the cluster with id [" + localNodeId + "]");
                } catch (ClusterCoordinationException e) {
                    inactivityTime = System.currentTimeMillis();
                    log.error("Node with ID " + localNodeId + " in group " + localGroupId + " could not join to the " +
                              "cluster due to " + e.getMessage() + " . Will retry in 5 seconds", e);
                    retryClusterJoin = true;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        log.error("Error in waiting for cluster join due to " + e1.getMessage(), e1);
                    }
                }
            }
        } while (retryClusterJoin);

    }

    @Override
    public void registerEventListener(MemberEventListener memberEventListener) {
        // Register listener for membership changes
        memberEventListener.setGroupId(localGroupId);
        rdbmsMemberEventProcessor.addEventListener(memberEventListener);
    }

    /**
     * Marks the specified node as unresponsive.
     *
     * @param nodeId  the ID of the node to be marked as unresponsive
     * @param groupId the ID of the group to which the node belongs
     */
    public void setUnresponsiveness(String nodeId, String groupId) {
        rdbmsMemberEventProcessor.setMemberUnresponsiveIfNeeded(nodeId, groupId, true);
    }

    private String getNodeId() {

        String nodeId = System.getProperty(NODE_ID);
        if (StringUtils.isEmpty(nodeId)) {
            nodeId = System.getenv(NODE_ID);
            if (StringUtils.isEmpty(nodeId)) {
                Object nodeIdObject = ConfigParser.getParsedConfigs().get(CLUSTER_CONFIG + "." + NODE_ID_CONFIG_NAME);
                if (nodeIdObject != null) {
                    nodeId = nodeIdObject.toString();
                } else {
                    nodeId = generateRandomId();
                }
            }
        }
        System.setProperty(NODE_ID_SYSTEM_PROPERTY, nodeId);
        return nodeId;
    }

    private String generateRandomId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Gives the id of this node.
     * @return The Node ID of this node.
     */
    public String getThisNodeId() {
        return localNodeId;
    }

    /**
     * This class will schedule and execute coordination tasks
     */
    public class HeartBeatExecutionTask implements Runnable {
        private CoordinatorElectionTask coordinatorElectionTask;
        private long lastHeartbeatFinishedTime;

        public HeartBeatExecutionTask(boolean stillCoordinator) {
            coordinatorElectionTask = new CoordinatorElectionTask(localNodeId, localGroupId, stillCoordinator);
        }

        @Override
        public void run() {
            while (isCoordinatorTasksRunning) {
                try {
                    long currentHeartbeatStartedTime = System.currentTimeMillis();
                    coordinatorElectionTask.runCoordinationElectionTask(currentHeartbeatStartedTime);
                    long taskEndedTime = System.currentTimeMillis();
                    if (lastHeartbeatFinishedTime != 0 &&
                        ((taskEndedTime - (lastHeartbeatFinishedTime + heartBeatInterval))
                         >= heartbeatWarningMargin)) {
                        log.warn("The heartBeatInterval is in " + heartBeatInterval +
                                 " millis with a retry count of " + heartbeatMaxRetry + ". " +
                                 "But current heartbeat has happened after " +
                                 (currentHeartbeatStartedTime - lastHeartbeatFinishedTime) +
                                 " millis from the last heartbeat, and took " +
                                 (taskEndedTime - currentHeartbeatStartedTime) +
                                 " millis to run CoordinationElection on the database at " +
                                 currentHeartbeatStartedTime +
                                 ". Please increase the heartBeat interval or the retry count.");
                    }
                    lastHeartbeatFinishedTime = currentHeartbeatStartedTime;
                    if (lastHeartbeatFinishedTime + heartBeatInterval - taskEndedTime > 5) {
                        Thread.sleep(lastHeartbeatFinishedTime + heartBeatInterval - taskEndedTime);
                    }
                } catch (Throwable t) {
                    log.error("Error occurred while performing coordinator tasks. " + t.getMessage(), t);
                }
            }
        }
    }

    /**
     * For each member, this class will run in a separate thread.
     */
    private class CoordinatorElectionTask {

        /**
         * Current state of the node.
         */
        private NodeState currentNodeState;

        /**
         * Previous state of the node.
         */
        private NodeState previousNodeState;

        /**
         * Used to uniquely identify a node in the cluster.
         */
        private String localNodeId;

        /**
         * Used to uniquely identify the group ID in the cluster.
         */
        private String localGroupId;

        /**
         * Executor service used to communicate with the database.
         */
        private ExecutorService dbCommunicatorExecutor = Executors.newSingleThreadExecutor();

        /**
         * Constructor.
         *
         * @param nodeId           - node ID of the current node
         * @param groupId          - group ID of the current group
         * @param stillCoordinator - whether this node is still a coordinator
         */
        private CoordinatorElectionTask(String nodeId, String groupId, boolean stillCoordinator) {
            this.localGroupId = groupId;
            this.localNodeId = nodeId;
            if (stillCoordinator) {
                this.currentNodeState = NodeState.COORDINATOR;
                this.previousNodeState = NodeState.COORDINATOR;
            } else {
                this.currentNodeState = NodeState.MEMBER;
                this.previousNodeState = NodeState.MEMBER;
            }

        }

        public void runCoordinationElectionTask(long currentHeartbeatTime) {
            try {
                if (!previousNodeState.equals(currentNodeState)) {
                    log.info("Current node state changed from: " + previousNodeState + " to: " + currentNodeState);
                    previousNodeState = currentNodeState;
                }
                long timeTakenForMemberTasks[] = new long[4];
                long timeTakenForCoordinatorTasks[] = new long[5];
                switch (currentNodeState) {
                    case MEMBER:
                        performMemberTask(currentHeartbeatTime, timeTakenForMemberTasks);
                        break;
                    case COORDINATOR:
                        performCoordinatorTask(currentHeartbeatTime, timeTakenForCoordinatorTasks);
                        break;
                }
                if (rdbmsMemberEventProcessor.isMemberUnresponsive()) {
                    log.info("Initiating unresponsive member recovery process for node " + localNodeId);
                    rdbmsMemberEventProcessor.setMemberUnresponsiveIfNeeded(localNodeId, localGroupId
                            , false);
                    rdbmsMemberEventProcessor.setMemberRejoined(localNodeId, localGroupId);
                }
                long clusterTaskEndingTime = System.currentTimeMillis();
                if (log.isDebugEnabled() && clusterTaskEndingTime - currentHeartbeatTime > 1000) {
                    log.debug("Cluster task took " +
                              (clusterTaskEndingTime - currentHeartbeatTime) + " millis to complete on " +
                              currentNodeState + " node at " + clusterTaskEndingTime);
                    switch (currentNodeState) {
                        case MEMBER:
                            log.debug("The time taken to execute tasks in milliseconds at timestamp: " +
                                      clusterTaskEndingTime +
                                      "\nupdateNodeHeartBeat(): " + timeTakenForMemberTasks[0] +
                                      "\ncheckIfCoordinatorValid(): " + timeTakenForMemberTasks[1] +
                                      "\nremoveCoordinator() if coordinator invalid: " + timeTakenForMemberTasks[2] +
                                      "\nperformElectionTask() if coordinator invalid: " + timeTakenForMemberTasks[3]);
                            break;
                        case COORDINATOR:
                            log.debug("The time taken to execute tasks in milliseconds at timestamp:" +
                                      clusterTaskEndingTime +
                                      "\nupdateCoordinatorHeartbeat(): " + timeTakenForCoordinatorTasks[0] +
                                      "\nupdateNodeHeartBeat() if still coordinator: " + timeTakenForCoordinatorTasks[1] +
                                      "\ngetAllNodeData() if still coordinator: " + timeTakenForCoordinatorTasks[2] +
                                      "\nfindAddedRemovedMembers() if still coordinator: " +
                                      timeTakenForCoordinatorTasks[3] +
                                      "\nperformElectionTask() if NOT still coordinator: " +
                                      timeTakenForCoordinatorTasks[4]);
                            break;
                        default:
                            log.error("No valid coordinator state found");
                    }
                }
                // We are catching throwable to avoid subsequent executions getting suppressed
            } catch (Throwable e) {
                currentNodeState = NodeState.MEMBER;
                try {
                    // Sleep for the duration of the inactiveIntervalAfterUnresponsive to allow time for the database to
                    // recover and give other nodes to take over the coordinator role and/or remove the node from the
                    // group.
                    Thread.sleep(inactiveIntervalAfterUnresponsive);
                } catch (InterruptedException ex) {
                    // ignore
                }
            }
        }

        /**
         * Perform DB operations with a timeout.
         *
         * @param task the callable task to be performed
         * @param <T>  the return type of the task
         * @return the result of the task
         * @throws ClusterCoordinationException if the task execution takes more time than the max DB timeout interval
         */
        public <T> T performDBOperationsWithTimeout(Callable<T> task) throws ClusterCoordinationException {
            Future<T> future = dbCommunicatorExecutor.submit(task);
            try {
                return future.get(maxDBReadTime, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                try {
                    future.cancel(true); // Cancel the task if it exceeds the max DB timeout interval
                } catch (Exception ex) {
                    //ignore
                }
                throw new ClusterCoordinationException("Database connection turned unresponsive. "
                        + "Please increase the heartbeat interval or verify the database connection.", e);
            }
        }

        /**
         * Perform periodic task that should be done by a MEMBER node.
         *
         * @throws ClusterCoordinationException
         * @throws InterruptedException
         */
        private void performMemberTask(long currentHeartbeatTime, long[] timeTakenForMemberTasks)
                throws ClusterCoordinationException, InterruptedException {
            long taskStartTime = System.currentTimeMillis();
            long taskEndTime;
            try {
                performDBOperationsWithTimeout(() -> {
                    updateNodeHeartBeat(currentHeartbeatTime);
                    return null;
                });
            } catch (ClusterCoordinationException e) {
                handleDatabaseDelay(localNodeId, localGroupId, e, "Error updating node heartbeat.");
                throw e;
            }
            taskEndTime = System.currentTimeMillis();
            timeTakenForMemberTasks[0] = taskEndTime - taskStartTime;
            taskStartTime = taskEndTime;
            boolean coordinatorValid;
            try {
                coordinatorValid = performDBOperationsWithTimeout(() ->
                        communicationBusContext.checkIfCoordinatorValid
                                (localGroupId, localNodeId, heartbeatMaxRetryInterval, currentHeartbeatTime)
                );
            } catch (ClusterCoordinationException e) {
                handleDatabaseDelay(localNodeId, localGroupId, e, "Error checking if coordinator is valid.");
                throw e;
            }
            taskEndTime = System.currentTimeMillis();
            timeTakenForMemberTasks[1] = taskEndTime - taskStartTime;
            if (!coordinatorValid) {
                taskStartTime = taskEndTime;
                try {
                    performDBOperationsWithTimeout(() -> {
                        communicationBusContext.removeCoordinator(localGroupId, heartbeatMaxRetryInterval
                                , currentHeartbeatTime);
                        return null;
                    });
                } catch (ClusterCoordinationException e) {
                    handleDatabaseDelay(localNodeId, localGroupId, e, "Error removing coordinator for group "
                            + localGroupId);
                    throw e;
                }
                taskEndTime = System.currentTimeMillis();
                timeTakenForMemberTasks[2] = taskEndTime - taskStartTime;
                taskStartTime = taskEndTime;
                performElectionTask(currentHeartbeatTime);
                taskEndTime = System.currentTimeMillis();
                timeTakenForMemberTasks[3] = taskEndTime - taskStartTime;
            }
        }

        /**
         * Try to update the heart beat entry for local node in the DB. If the entry is deleted by the coordinator,
         * this will recreate the entry.
         *
         * @throws ClusterCoordinationException
         */
        private void updateNodeHeartBeat(long currentHeartbeatTime) throws ClusterCoordinationException {
            boolean heartbeatEntryExists = communicationBusContext.updateNodeHeartbeat(localNodeId, localGroupId,
                                                                                       currentHeartbeatTime);
            if (!heartbeatEntryExists) {
                communicationBusContext.createNodeHeartbeatEntry(localNodeId, localGroupId);
            }
        }

        /**
         * Perform periodic task that should be done by a Coordinating node.
         *
         * @throws ClusterCoordinationException
         * @throws InterruptedException
         */
        private void performCoordinatorTask(long currentHeartbeatTime, long[] timeTakenForCoordinatorTasks)
                throws ClusterCoordinationException, InterruptedException {
            // Try to update the coordinator heartbeat
            long taskStartTime = System.currentTimeMillis();
            long taskEndTime;
            boolean stillCoordinator;
            try {
                stillCoordinator = performDBOperationsWithTimeout(() ->
                        communicationBusContext.updateCoordinatorHeartbeat
                                (localNodeId, localGroupId, currentHeartbeatTime));
            } catch (ClusterCoordinationException e) {
                handleDatabaseDelay(localNodeId, localGroupId, e,
                        "Error updating coordinator heartbeat in LEADER_STATUS_TABLE due to database delay."
                                + " Stopping coordinated tasks for this node. Please increase the heartbeat interval or"
                                + " verify the database connection.");
                throw e;
            }
            taskEndTime = System.currentTimeMillis();
            timeTakenForCoordinatorTasks[0] = taskEndTime - taskStartTime;
            taskStartTime = taskEndTime;
            if (stillCoordinator) {
                try {
                    performDBOperationsWithTimeout(() -> {
                        updateNodeHeartBeat(currentHeartbeatTime);
                        return null;
                    });
                } catch (ClusterCoordinationException e) {
                   handleDatabaseDelay(localNodeId, localGroupId, e,
                           "Error updating node heartbeat in CLUSTER_NODE_STATUS_TABLE due to database delay."
                                   + " Stopping coordinated tasks for this node. Please increase the heartbeat interval"
                                   + " or verify the database connection.");
                    throw e;
                }
                taskEndTime = System.currentTimeMillis();
                timeTakenForCoordinatorTasks[1] = taskEndTime - taskStartTime;
                taskStartTime = taskEndTime;

                List<NodeDetail> allNodeInformation;
                try {
                    allNodeInformation = performDBOperationsWithTimeout(() ->
                            communicationBusContext.getAllNodeData(localGroupId));
                } catch (ClusterCoordinationException e) {
                    handleDatabaseDelay(localNodeId, localGroupId, e, "Error retrieving all node data from"
                            + " LEADER_STATUS_TABLE and CLUSTER_NODE_STATUS_TABLE due to database delay."
                            + " Stopping coordinated tasks for this node. Please increase the heartbeat interval or"
                            + " verify the database connection.");
                   throw e;
                }
                taskEndTime = System.currentTimeMillis();
                timeTakenForCoordinatorTasks[2] = taskEndTime - taskStartTime;
                taskStartTime = taskEndTime;
                findAddedRemovedMembers(allNodeInformation, currentHeartbeatTime);
                taskEndTime = System.currentTimeMillis();
                timeTakenForCoordinatorTasks[3] = taskEndTime - taskStartTime;
            } else {
                log.info("Found current node (nodeId: " + localNodeId + ") being removed from coordinator for " +
                         "the group " + localGroupId);
                performElectionTask(currentHeartbeatTime);
                taskEndTime = System.currentTimeMillis();
                timeTakenForCoordinatorTasks[4] = taskEndTime - taskStartTime;
            }

        }

        /**
         * Finds the newly added and removed nodes to the group.
         *
         * @param allNodeInformation all the node information of the group
         * @param currentTimeMillis  current timestamp
         */
        private void findAddedRemovedMembers(List<NodeDetail> allNodeInformation, long currentTimeMillis) {
            List<String> allActiveNodeIds = getNodeIds(allNodeInformation);
            List<NodeDetail> removedNodeDetails = new ArrayList<>();
            List<String> newNodes = new ArrayList<String>();
            List<String> removedNodes = new ArrayList<String>();
            for (NodeDetail nodeDetail : allNodeInformation) {
                long heartbeatAge = currentTimeMillis - nodeDetail.getLastHeartbeat();
                String nodeId = nodeDetail.getNodeId();
                if (heartbeatAge >= heartbeatMaxRetryInterval) {
                    removedNodes.add(nodeId);
                    allActiveNodeIds.remove(nodeId);
                    removedNodeDetails.add(nodeDetail);
                    try {
                        performDBOperationsWithTimeout(() -> {
                            communicationBusContext.removeNode(nodeId, localGroupId);
                            return null;
                        });
                    } catch (ClusterCoordinationException e) {
                        handleDatabaseDelay(nodeId, localGroupId, e, "Error removing node " + nodeId
                                + " from group " + localGroupId + " due to database delay.");
                        throw e;
                    }
                } else if (nodeDetail.isNewNode()) {
                    newNodes.add(nodeId);
                    try {
                        performDBOperationsWithTimeout(() -> {
                            communicationBusContext.markNodeAsNotNew(nodeId, localGroupId);
                            return null;
                        });
                    } catch (ClusterCoordinationException e) {
                        handleDatabaseDelay(nodeId, localGroupId, e, "Error marking node as not"
                                + " new for nodeId: " + nodeId + " in group: " + localGroupId);
                        throw e;

                    }
                }
            }

            notifyAddedMembers(newNodes, allActiveNodeIds);
            notifyRemovedMembers(removedNodes, allActiveNodeIds, removedNodeDetails);
        }

        /**
         * Handles the database delay.
         *
         * @param nodeId      node ID of the current node
         * @param groupId group ID of the current group
         * @param e           exception occurred
         * @param logMessage  log message
         */
        private void handleDatabaseDelay(String nodeId, String groupId, Exception e, String logMessage) {
            log.warn(logMessage + " Make task Sleep for the duration of : " + inactiveIntervalAfterUnresponsive , e);
            setUnresponsiveness(nodeId, groupId);
        }

        /**
         * Notifies the members in the group about the newly added nodes.
         *
         * @param newNodes         The list of newly added members to the group
         * @param allActiveNodeIds all the node IDs of the current group
         */
        private void notifyAddedMembers(List<String> newNodes, List<String> allActiveNodeIds) {
            for (String newNode : newNodes) {
                if (log.isDebugEnabled()) {
                    log.debug("Member added " + StringUtil.removeCRLFCharacters(newNode) + "to group " +
                              StringUtil.removeCRLFCharacters(localGroupId));
                }
                try {
                    performDBOperationsWithTimeout(() -> {
                        rdbmsMemberEventProcessor.notifyMembershipEvent(newNode, localGroupId, allActiveNodeIds,
                                MemberEventType.MEMBER_ADDED);
                        return null;
                    });
                } catch (ClusterCoordinationException e) {
                    handleDatabaseDelay(newNode, localGroupId, e
                            , "Error notifying membership event for new node " + newNode);
                    throw e;
                }
            }
        }

        /**
         * Stores the removed member detail in the database.
         *
         * @param allActiveNodeIds   all the node IDs of the current group
         * @param removedNodeDetails node details of the removed nodes
         */
        private void storeRemovedMemberDetails(List<String> allActiveNodeIds, List<NodeDetail> removedNodeDetails) {
            for (NodeDetail nodeDetail : removedNodeDetails) {
                try {
                    performDBOperationsWithTimeout(() -> {
                        communicationBusContext.insertRemovedNodeDetails(nodeDetail.getNodeId()
                                , nodeDetail.getGroupId(), allActiveNodeIds);
                        return null;
                    });
                } catch (ClusterCoordinationException e) {
                    handleDatabaseDelay(nodeDetail.getNodeId(), nodeDetail.getGroupId(), e
                            , "Error inserting removed node details for node " + nodeDetail.getNodeId());
                    throw e;
                }
            }
        }

        /**
         * Notifies the members in the group about the removed nodes from the group.
         *
         * @param removedNodes     The list of removed membwes from the group
         * @param allActiveNodeIds all the node IDs of the current group
         */
        private void notifyRemovedMembers(List<String> removedNodes, List<String> allActiveNodeIds,
                                          List<NodeDetail> removedNodeDetails) {
            storeRemovedMemberDetails(allActiveNodeIds, removedNodeDetails);
            for (String removedNode : removedNodes) {
                if (log.isDebugEnabled()) {
                    log.debug("Member removed " + StringUtil.removeCRLFCharacters(removedNode) + "from group "
                              + StringUtil.removeCRLFCharacters(localGroupId));
                }
                try {
                    performDBOperationsWithTimeout(() -> {
                        rdbmsMemberEventProcessor.notifyMembershipEvent(removedNode, localGroupId, allActiveNodeIds,
                                MemberEventType.MEMBER_REMOVED);
                        return null;
                    });
                } catch (ClusterCoordinationException e) {
                    handleDatabaseDelay(removedNode, localGroupId, e
                            , "Error notifying membership event for removed node " + removedNode);
                    throw e;
                }
            }
        }

        /**
         * Perform new coordinator election task.
         *
         * @throws InterruptedException
         */
        private void performElectionTask(long currentHeartbeatTime) throws InterruptedException {
            try {
                this.currentNodeState = tryToElectSelfAsCoordinator(currentHeartbeatTime);
            } catch (ClusterCoordinationException e) {
                throw e;
            }
        }

        /**
         * Try to elect local node as the coordinator by creating the coordinator entry.
         *
         * @return next NodeState
         * @throws ClusterCoordinationException
         */
        private NodeState tryToElectSelfAsCoordinator(long currentHeartbeatTime)
                throws ClusterCoordinationException {
            NodeState nodeState;
            boolean electedAsCoordinator;
            try {
                electedAsCoordinator = performDBOperationsWithTimeout(()
                        -> communicationBusContext.createCoordinatorEntry(localNodeId, localGroupId));
            } catch (ClusterCoordinationException e) {
                handleDatabaseDelay(localNodeId, localGroupId, e,
                        "Error occurred while trying to elect self as coordinator for group " + localGroupId);
                throw e;
            }
            if (electedAsCoordinator) {
                log.info("Elected current node (nodeID: " + localNodeId + ") as the coordinator for the group " +
                         localGroupId);
                List<NodeDetail> allNodeInformation;
                try {
                    allNodeInformation = performDBOperationsWithTimeout(()
                            -> communicationBusContext.getAllNodeData(localGroupId));
                } catch (ClusterCoordinationException e) {
                    handleDatabaseDelay(localNodeId, localGroupId, e
                            , "Error retrieving all node data from the database.");
                    throw e;
                }
                findAddedRemovedMembers(allNodeInformation, currentHeartbeatTime);
                nodeState = NodeState.COORDINATOR;
                // notify nodes about coordinator change
                List<String> nodeIdentifiers = new ArrayList<>();
                for (NodeDetail nodeDetail : allNodeInformation) {
                    nodeIdentifiers.add(nodeDetail.getNodeId());
                }
               try {
                   performDBOperationsWithTimeout(() -> {
                       rdbmsMemberEventProcessor.notifyMembershipEvent(localNodeId, localGroupId, nodeIdentifiers,
                               MemberEventType.COORDINATOR_CHANGED);
                       return null;
                   });
               } catch (ClusterCoordinationException e) {
                   handleDatabaseDelay(localNodeId, localGroupId, e
                           , "Error notifying membership event for coordinator change.");
                   throw e;
               }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Election resulted in current node becoming a " + NodeState.MEMBER
                              + " node in group " + localGroupId);
                }
                nodeState = NodeState.MEMBER;
            }
            return nodeState;
        }

        /**
         * Get the node IDs of the current group.
         *
         * @return node IDs of the current group
         * @throws ClusterCoordinationException
         */
        public List<String> getAllNodeIdentifiers() throws ClusterCoordinationException {
            List<NodeDetail> allNodeInformation = communicationBusContext.getAllNodeData(localGroupId);
            return getNodeIds(allNodeInformation);
        }

        /**
         * Return a list of node ids from the heartbeat data list.
         *
         * @param allHeartbeatData list of heartbeat data
         * @return list of node IDs
         */
        private List<String> getNodeIds(List<NodeDetail> allHeartbeatData) {
            List<String> allNodeIds = new ArrayList<String>(allHeartbeatData.size());
            for (NodeDetail nodeDetail : allHeartbeatData) {
                allNodeIds.add(nodeDetail.getNodeId());
            }
            return allNodeIds;
        }
    }
}
