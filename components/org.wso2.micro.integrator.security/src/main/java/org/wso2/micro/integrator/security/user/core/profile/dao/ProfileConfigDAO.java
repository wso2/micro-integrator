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
package org.wso2.micro.integrator.security.user.core.profile.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.security.user.core.UserCoreConstants;
import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.profile.ProfileConfiguration;
import org.wso2.micro.integrator.security.user.core.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

public class ProfileConfigDAO {

    private static Log log = LogFactory.getLog(DatabaseUtil.class);

    private DataSource dataSource = null;

    private int tenantId;

    public ProfileConfigDAO(DataSource dataSource, int tenantId) {
        this.dataSource = dataSource;
        this.tenantId = tenantId;
    }

    public void addProfileConfig(ProfileConfiguration profileConfig) throws UserStoreException {
        Connection dbConnection = null;
        try {
            dbConnection = dataSource.getConnection();
            dbConnection.setAutoCommit(false);
            addProfileConfig(dbConnection, profileConfig);
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage = "Database Error - " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeConnection(dbConnection);
        }
    }

    public void addProfileConfig(ProfileConfiguration[] profileConfigs) throws UserStoreException {
        Connection dbConnection = null;
        try {
            dbConnection = dataSource.getConnection();
            dbConnection.setAutoCommit(false);
            for (ProfileConfiguration profileConfig : profileConfigs) {
                addProfileConfig(dbConnection, profileConfig);
            }
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage = "Database Error - " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeConnection(dbConnection);
        }
    }

    public void updateProfileConfig(ProfileConfiguration profileConfig) throws UserStoreException {
        Connection dbConnection = null;
        try {
            dbConnection = dataSource.getConnection();
            dbConnection.setAutoCommit(false);
            deleteProfileConfig(dbConnection, profileConfig.getProfileName(), profileConfig.getDialectName());
            addProfileConfig(dbConnection, profileConfig);
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage = "Database Error - " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeConnection(dbConnection);

        }
    }

    public void deleteProfileConfig(ProfileConfiguration profileConfig) throws UserStoreException {
        Connection dbConnection = null;
        try {
            dbConnection = dataSource.getConnection();
            dbConnection.setAutoCommit(false);
            deleteProfileConfig(dbConnection, profileConfig.getProfileName(), profileConfig.getDialectName());
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage = "Database Error - " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeConnection(dbConnection);
        }
    }

    // um_claim_uri, um_profile_name, um_behaviou
    public Map<String, ProfileConfiguration> loadProfileConfigs() throws UserStoreException {
        Connection dbConnection = null;
        Map<String, ProfileConfiguration> map = new HashMap<String, ProfileConfiguration>();
        PreparedStatement prepStmt = null;
        try {
            dbConnection = dataSource.getConnection();
            prepStmt = dbConnection
                    .prepareStatement(ProfileDBConstant.GET_ALL_PROFILE_CONFIGS);
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, tenantId);
            prepStmt.setInt(3, tenantId);
            prepStmt.setInt(4, tenantId);
            ResultSet rs = prepStmt.executeQuery();
            while (rs.next()) {
                String claimUri = rs.getString(1);
                String profileName = rs.getString(2);
                short behavior = rs.getShort(3);
                String dialectUri = rs.getString(4);
                ProfileConfiguration profConfig = map.get(profileName);
                if (profConfig == null) {
                    profConfig = new ProfileConfiguration();
                    map.put(profileName, profConfig);
                }
                profConfig.setDialectName(dialectUri);
                profConfig.setProfileName(profileName);
                if (behavior == UserCoreConstants.BEHAVIOUR_HIDDEN) {
                    profConfig.addHiddenClaim(claimUri);
                } else if (behavior == UserCoreConstants.BEHAVIOUR_INHERITED) {
                    profConfig.addInheritedClaim(claimUri);
                } else if (behavior == UserCoreConstants.BEHAVIOUR_OVERRIDDEN) {
                    profConfig.addOverriddenClaim(claimUri);
                } else {
                    assert (false);
                }
            }
        } catch (SQLException e) {
            String errorMessage = "Database Error - " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, prepStmt);
        }
        return map;
    }

    protected void addProfileConfig(Connection dbConnection, ProfileConfiguration profileConfig)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            int dialectId = -1;

            String profileName = profileConfig.getProfileName();
            prepStmt = dbConnection.prepareStatement(ProfileDBConstant.GET_DIALECT_ID);
            prepStmt.setString(1, profileConfig.getDialectName());
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                dialectId = rs.getInt(1);
            }
            rs.close();
            prepStmt.close();

            if (dialectId == -1) {
                throw new UserStoreException("Please add the dialect URI first.");
            }

            prepStmt = dbConnection.prepareStatement(ProfileDBConstant.ADD_PROFILE_CONFIG);
            prepStmt.setString(1, profileConfig.getProfileName());
            prepStmt.setInt(2, dialectId);
            prepStmt.setInt(3, tenantId);
            prepStmt.executeUpdate();
            prepStmt.close();

            int profileId = -1;
            prepStmt = dbConnection.prepareStatement(ProfileDBConstant.GET_PROFILE_ID);
            prepStmt.setString(1, profileName);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                profileId = rs.getInt(1);
            }
            rs.close();
            prepStmt.close();

            Map<String, Integer> ids = getClaimUris(dbConnection);
            prepStmt = dbConnection.prepareStatement(ProfileDBConstant.ADD_CLAIM_BEHAVIOR);
            addToAddBatch(profileConfig.getHiddenClaims(), profileId, ids,
                    UserCoreConstants.BEHAVIOUR_HIDDEN, prepStmt);
            addToAddBatch(profileConfig.getOverriddenClaims(), profileId, ids,
                    UserCoreConstants.BEHAVIOUR_OVERRIDDEN, prepStmt);
            addToAddBatch(profileConfig.getInheritedClaims(), profileId, ids,
                    UserCoreConstants.BEHAVIOUR_INHERITED, prepStmt);
            prepStmt.executeBatch();
        } catch (SQLException e) {
            String errorMessage = "Database Error - " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    public void deleteProfileConfig(Connection dbConnection, String profileName, String dialect)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection
                    .prepareStatement(ProfileDBConstant.DELETE_CLAIM_BEHAVIOR);
            prepStmt.setString(1, profileName);
            prepStmt.setString(2, dialect);
            prepStmt.setInt(3, tenantId);
            prepStmt.setInt(4, tenantId);
            prepStmt.setInt(5, tenantId);
            int ival = prepStmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Deleted claim behavior numbers :: " + ival);
            }

            prepStmt.close();
            prepStmt = dbConnection
                    .prepareStatement(ProfileDBConstant.DELETE_PROFILE_CONFIG);
            prepStmt.setString(1, profileName);
            prepStmt.setString(2, dialect);
            prepStmt.setInt(3, tenantId);
            prepStmt.setInt(4, tenantId);
            ival = prepStmt.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Deleted profile names :: " + ival);
            }

            prepStmt.close();
        } catch (SQLException e) {
            String errorMessage = "Database Error - " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    protected Map<String, Integer> getClaimUris(Connection dbConnection) throws UserStoreException {
        Map<String, Integer> map = new HashMap<String, Integer>();
        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(ProfileDBConstant.GET_CLAIM_IDS);
            prepStmt.setInt(1, tenantId);
            ResultSet rs = prepStmt.executeQuery();
            while (rs.next()) {
                int claimid = rs.getInt(1);
                String claimuri = rs.getString(2);
                map.put(claimuri, claimid);
            }
        } catch (SQLException e) {
            String errorMessage = "Database Error - " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
        return map;
    }

    private void addToAddBatch(List<String> lst, int profileId, Map<String, Integer> ids,
                               short behavior, PreparedStatement prepStmt) throws SQLException {
        for (Iterator<String> ite = lst.iterator(); ite.hasNext(); ) {
            String claimUri = ite.next();
            if (claimUri == null || ids.get(claimUri) == null) {
                continue;
            }
            prepStmt.setInt(1, profileId); //profName
            prepStmt.setInt(2, ids.get(claimUri));
            prepStmt.setShort(3, behavior);
            prepStmt.setInt(4, tenantId);
            prepStmt.addBatch();
        }
    }

}
