/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.security.user.core.authorization;

import java.util.ArrayList;
import java.util.List;

public class PermissionTreeUtil {

    public static List<String> toComponenets(String path) {
        String[] results = path.split("/");
        List<String> resultArr = new ArrayList<String>();
        int start = 0;
        //Removing the empty strings at start, but split javadoc
        //says it removes empty spaces
        if (results.length > 0 && results[0].length() == 0) {
            start = 1;
        }
        for (int i = start; i < results.length; i++) {
            resultArr.add(results[i]);
        }
        return resultArr;
    }

    public static TreeNode.Permission actionToPermission(String action) {
        if ("add".equalsIgnoreCase(action)) {
            return TreeNode.Permission.ADD;
        } else if ("get".equalsIgnoreCase(action)) {
            return TreeNode.Permission.GET;
        } else if ("delete".equalsIgnoreCase(action)) {
            return TreeNode.Permission.DELETE;
        } else if ("write".equalsIgnoreCase(action)) {
            return TreeNode.Permission.ADD;
        } else if ("read".equalsIgnoreCase(action)) {
            return TreeNode.Permission.GET;
        } else if ("edit".equalsIgnoreCase(action)) {
            return TreeNode.Permission.EDIT;
        } else if ("login".equalsIgnoreCase(action)) {
            return TreeNode.Permission.LOGIN;
        } else if ("manage-configuration".equalsIgnoreCase(action)) {
            return TreeNode.Permission.MAN_CONFIG;
        } else if ("manage-lc-configuration".equalsIgnoreCase(action)) {
            return TreeNode.Permission.MAN_LC_CONFIG;
        } else if ("manage-security".equalsIgnoreCase(action)) {
            return TreeNode.Permission.MAN_SEC;
        } else if ("upload-services".equalsIgnoreCase(action)) {
            return TreeNode.Permission.UP_SERV;
        } else if ("manage-services".equalsIgnoreCase(action)) {
            return TreeNode.Permission.MAN_SERV;
        } else if ("manage-mediation".equalsIgnoreCase(action)) {
            return TreeNode.Permission.MAN_MEDIA;
        } else if ("monitor-system".equalsIgnoreCase(action)) {
            return TreeNode.Permission.MON_SYS;
        } else if ("http://www.wso2.org/projects/registry/actions/get".equalsIgnoreCase(action)) {
            return TreeNode.Permission.GET;
        } else if ("http://www.wso2.org/projects/registry/actions/add".equalsIgnoreCase(action)) {
            return TreeNode.Permission.ADD;
        } else if ("http://www.wso2.org/projects/registry/actions/delete".equalsIgnoreCase(action)) {
            return TreeNode.Permission.DELETE;
        } else if ("authorize".equalsIgnoreCase(action)) {
            return TreeNode.Permission.AUTHORIZE;
        } else if ("delegate-identity".equalsIgnoreCase(action)) {
            return TreeNode.Permission.DEL_ID;
        } else if ("invoke-service".equalsIgnoreCase(action)) {
            return TreeNode.Permission.INV_SER;
        } else if ("ui.execute".equalsIgnoreCase(action)) {
            return TreeNode.Permission.UI_EXECUTE;
        } else if ("subscribe".equalsIgnoreCase(action)) {
            return TreeNode.Permission.SUBSCRIBE;
        } else if ("publish".equalsIgnoreCase(action)) {
            return TreeNode.Permission.PUBLISH;
        } else if ("browse".equalsIgnoreCase(action)) {
            return TreeNode.Permission.BROWSE;
        } else if ("consume".equalsIgnoreCase(action)) {
            return TreeNode.Permission.CONSUME;
        } else if ("changePermission".equalsIgnoreCase(action)) {
            return TreeNode.Permission.CHANGE_PERMISSION;
        } else if ("SendMessage".equalsIgnoreCase(action)) {
            return TreeNode.Permission.SQS_SEND_MESSAGE;
        } else if ("ReceiveMessage".equalsIgnoreCase(action)) {
            return TreeNode.Permission.SQS_RECEIVE_MESSAGE;
        } else if ("DeleteMessage".equalsIgnoreCase(action)) {
            return TreeNode.Permission.SQS_DELETE_MESSAGE;
        } else if ("ChangeMessageVisibility".equalsIgnoreCase(action)) {
            return TreeNode.Permission.SQS_CHANGE_MESSAGE_VISIBILITY;
        } else if ("GetQueueAttributes".equalsIgnoreCase(action)) {
            return TreeNode.Permission.SQS_GET_QUEUE_ATTRIBUTES;
        }


        throw new IllegalArgumentException("Invalid action : " + action);
    }

}
