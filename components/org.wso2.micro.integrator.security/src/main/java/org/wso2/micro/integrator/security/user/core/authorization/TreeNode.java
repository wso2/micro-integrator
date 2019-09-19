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


import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.core.util.UserCoreUtil;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A node in the Tree structure used to maintain hierarchical security permissions. The growth
 * of the tree is on the order of explicit permission statements, and not on the number of
 * resources whose permissions are maintained.
 */
public class TreeNode {

    /**
     * The name of the node - For the Registry, this would be the name of a Collection/Resource
     */
    private String name;
    /**
     * The children of this node - maintained on a Map by the names
     */
    private Map<String, TreeNode> children = new HashMap<String, TreeNode>();
    /**
     * Explicit allow permission for specific users
     */
    private Map<String, BitSet> userAllowPermissions = new HashMap<String, BitSet>();
    /**
     * Explicit deny permission for specific users
     */
    private Map<String, BitSet> userDenyPermissions = new HashMap<String, BitSet>();
    /**
     * Explicit allow permission for specific roles
     */
    private Map<String, BitSet> roleAllowPermissions = new HashMap<String, BitSet>();
    /**
     * Explicit deny permission for specific roles
     */
    private Map<String, BitSet> roleDenyPermissions = new HashMap<String, BitSet>();
    /**
     * Constructor
     *
     * @param name the name of the TreeNode
     */
    TreeNode(String name) {
        this.name = name;
    }

    /**
     * Get the child by the given name
     *
     * @param name name of the child node
     * @return the child with the given name, or null
     */
    TreeNode getChild(String name) {
        return children.get(name);
    }

    /**
     * Is the 'user' authorized for the given permission on this node?
     *
     * @param user       the name of the user
     * @param permission the permission
     * @return Boolean.TRUE if authorized, Boolean.FALSE if not
     */
    public Boolean isUserAuthorized(String user, Permission permission) {
        BitSet bsAlow = userAllowPermissions.get(user);
        BitSet bsDeny = userDenyPermissions.get(user);
        if (bsAlow == null && bsDeny == null) {
            return null;
        } else if (bsDeny != null && bsDeny.get(permission.ordinal())) {
            return Boolean.FALSE;
        } else if (bsAlow != null && bsAlow.get(permission.ordinal())) {
            return Boolean.TRUE;
        }

        return null;
    }

    /**
     * Is the 'role' authorized for the given permission on this node?
     *
     * @param role       the name of the role
     * @param permission the permission
     * @return Boolean.TRUE if authorized, Boolean.FALSE if not
     */
    public Boolean isRoleAuthorized(String role, Permission permission) {
        BitSet bsAlow = roleAllowPermissions.get(modify(role));
        BitSet bsDeny = roleDenyPermissions.get(role);
        if (bsAlow == null && bsDeny == null) {
            return null;
        } else if (bsDeny != null && bsDeny.get(permission.ordinal())) {
            return Boolean.FALSE;
        } else if (bsAlow != null && bsAlow.get(permission.ordinal())) {
            return Boolean.TRUE;
        }

        return null;
    }

    /**
     * Grant explicit authorization to the 'user' on this node for permission
     *
     * @param user       the user who is granted authorization
     * @param permission the permission granted
     */
    public void authorizeUser(String user, Permission permission) {
        BitSet bsAllow = userAllowPermissions.get(user);
        if (bsAllow == null) {
            bsAllow = new BitSet();
            bsAllow.set(permission.ordinal());
            userAllowPermissions.put(user, bsAllow);
        } else {
            bsAllow.set(permission.ordinal());
        }

        BitSet bsDeny = userDenyPermissions.get(user);
        if (bsDeny != null) {
            bsDeny.clear(permission.ordinal());
        }
    }

    /**
     * Grant explicit authorization to the 'role' on this node for permission
     *
     * @param role       the role that is granted authorization
     * @param permission the permission granted
     */
    public void authorizeRole(String role, Permission permission) {
        BitSet bsAllow = roleAllowPermissions.get(modify(role));
        if (bsAllow == null) {
            bsAllow = new BitSet();
            bsAllow.set(permission.ordinal());
            roleAllowPermissions.put(modify(role), bsAllow);
        } else {
            bsAllow.set(permission.ordinal());
        }

        BitSet bsDeny = roleDenyPermissions.get(role);
        if (bsDeny != null) {
            bsDeny.clear(permission.ordinal());
        }
    }

    /**
     * Deny explicit authorization to the 'user' on this node for permission
     *
     * @param user       the user that is denied authorization
     * @param permission the permission denied
     */
    public void denyUser(String user, Permission permission) {
        BitSet bsDeny = userDenyPermissions.get(user);
        if (bsDeny == null) {
            bsDeny = new BitSet();
            bsDeny.set(permission.ordinal());
            userDenyPermissions.put(user, bsDeny);
        } else {
            bsDeny.set(permission.ordinal());
        }

        BitSet bsAllow = userAllowPermissions.get(user);
        if (bsAllow != null) {
            bsAllow.clear(permission.ordinal());
        }
    }

    /**
     * Deny explicit authorization to the 'role' on this node for permission
     *
     * @param role       the role that is denied authorization
     * @param permission the permission denied
     */
    public void denyRole(String role, Permission permission) {
        BitSet bsDeny = roleDenyPermissions.get(role);
        if (bsDeny == null) {
            bsDeny = new BitSet();
            bsDeny.set(permission.ordinal());
            roleDenyPermissions.put(role, bsDeny);
        } else {
            bsDeny.set(permission.ordinal());
        }

        BitSet bsAllow = roleAllowPermissions.get(modify(role));
        if (bsAllow != null) {
            bsAllow.clear(permission.ordinal());
        }
    }

    /**
     * Create the tree structure for the given paths array of nodes
     *
     * @param paths an array of hierarchical nodes to be created, in-order
     * @return the reference to the lowest decendent created
     */
    public TreeNode create(List<String> paths) {
        if (paths != null && !paths.isEmpty()) {
            String childName = paths.get(0);
            TreeNode tn = new TreeNode(childName);
            children.put(childName, tn);
            paths.remove(0);
            if (!paths.isEmpty()) {
                return tn.create(paths);
            } else {
                return tn;
            }
        } else {
            return this;
        }
    }

    /**
     * The name of the node
     *
     * @return node name
     */
    public String getName() {
        return name;
    }

    /**
     * The children of the node as a Map keyed by the name
     *
     * @return the children as a Map
     */
    public Map<String, TreeNode> getChildren() {
        return children;
    }

    //-------- getters --------
    public Map<String, BitSet> getUserAllowPermissions() {
        return userAllowPermissions;
    }

    public Map<String, BitSet> getUserDenyPermissions() {
        return userDenyPermissions;
    }

    public Map<String, BitSet> getRoleAllowPermissions() {
        return roleAllowPermissions;
    }

    public Map<String, BitSet> getRoleDenyPermissions() {
        return roleDenyPermissions;
    }

    /**
     * This will clear all permission maps. Also this will clear child maps.
     */
    public void clearNodes() {

        this.roleAllowPermissions.clear();
        this.roleDenyPermissions.clear();
        this.userAllowPermissions.clear();
        this.userDenyPermissions.clear();

        Map<String, TreeNode> children = this.getChildren();
        if (null != children) {
            for (Map.Entry<String, TreeNode> entry : children.entrySet()) {
                TreeNode node = entry.getValue();
                if (null != node) {
                    node.clearNodes();
                }
            }

            children.clear();
        }
    }

    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (null == this.name ? 0 : this.name.hashCode());
        hash = 31 * hash + (null == this.children ? 0 : this.children.hashCode());
        hash = 31 * hash + (null == this.userAllowPermissions ? 0 : this.userAllowPermissions.hashCode());
        hash = 31 * hash + (null == this.userDenyPermissions ? 0 : this.userDenyPermissions.hashCode());
        hash = 31 * hash + (null == this.roleAllowPermissions ? 0 : this.roleAllowPermissions.hashCode());
        hash = 31 * hash + (null == this.roleDenyPermissions ? 0 : this.roleDenyPermissions.hashCode());
        hash = 31 * hash + (null == this.roleDenyPermissions ? 0 : this.roleDenyPermissions.hashCode());
        return hash;
    }

    public static enum Permission {
        GET, ADD, DELETE, EDIT, LOGIN, MAN_CONFIG, MAN_LC_CONFIG, MAN_SEC, UP_SERV,
        MAN_SERV, MAN_MEDIA, MON_SYS, DEL_ID, AUTHORIZE, INV_SER, UI_EXECUTE, SUBSCRIBE, PUBLISH, CONSUME, CHANGE_PERMISSION, BROWSE,
        SQS_SEND_MESSAGE, SQS_RECEIVE_MESSAGE, SQS_DELETE_MESSAGE, SQS_CHANGE_MESSAGE_VISIBILITY, SQS_GET_QUEUE_ATTRIBUTES
    }

    private String modify(String name) {
        if (!name.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
            return name;
        }
        String domain = UserCoreUtil.extractDomainFromName(name);
        String nameWithoutDomain = UserCoreUtil.removeDomainFromName(name);
        String modifiedName = UserCoreUtil.addDomainToName(nameWithoutDomain, domain.toUpperCase());
        return modifiedName;
    }
}
