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

/**
 * The SearchResult holder
 */
class SearchResult {
    /**
     * This is an exact TreeNode match, or the last node found while
     * traversing the tree
     */
    private TreeNode lastNode;
    /**
     * The List of path segments not found on the tree, during the search
     */
    private List<String> unprocessedPaths;
    /**
     * The accumulated state of explicit permissions to the last node
     */
    private Boolean lastNodeAllowedAccess = Boolean.FALSE;

    private List<String> allowedEntities = new ArrayList<String>();
    private List<String> deniedEntities = new ArrayList<String>();

    SearchResult() {
    }

    SearchResult(TreeNode tn, List<String> up) {
        this.lastNode = tn;
        this.unprocessedPaths = up;
    }

    public TreeNode getLastNode() {
        return lastNode;
    }

    public void setLastNode(TreeNode lastNode) {
        this.lastNode = lastNode;
    }

    public List<String> getUnprocessedPaths() {
        return unprocessedPaths;
    }

    public void setUnprocessedPaths(List<String> unprocessedPaths) {
        this.unprocessedPaths = unprocessedPaths;
    }

    public Boolean getLastNodeAllowedAccess() {
        return lastNodeAllowedAccess;
    }

    public void setLastNodeAllowedAccess(Boolean lastNodeAllowedAccess) {
        this.lastNodeAllowedAccess = lastNodeAllowedAccess;
    }

    public List<String> getAllowedEntities() {
        return allowedEntities;
    }

    public void setAllowedEntities(List<String> allowedEntities) {
        this.allowedEntities = allowedEntities;
    }

    public List<String> getDeniedEntities() {
        return deniedEntities;
    }

    public void setDeniedEntities(List<String> deniedEntities) {
        this.deniedEntities = deniedEntities;
    }
}
