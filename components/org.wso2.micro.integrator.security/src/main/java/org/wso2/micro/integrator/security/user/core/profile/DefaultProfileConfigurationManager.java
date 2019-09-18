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
package org.wso2.micro.integrator.security.user.core.profile;

import org.wso2.micro.integrator.security.user.core.UserStoreException;
import org.wso2.micro.integrator.security.user.core.profile.dao.ProfileConfigDAO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

public class DefaultProfileConfigurationManager implements ProfileConfigurationManager {

    // private Map<String, List<ProfileConfiguration>> profileConfigs = new
    // ConcurrentHashMap<String, ProfileConfiguration>();
    private Map<String, ProfileConfiguration> profileConfigs = new ConcurrentHashMap<String, ProfileConfiguration>();
    private ProfileConfigDAO profileDAO = null;

    public DefaultProfileConfigurationManager(Map<String, ProfileConfiguration> profileConfigs,
                                              DataSource dataSource, int tenantId) {
        this.profileConfigs = new ConcurrentHashMap<String, ProfileConfiguration>();
        this.profileConfigs.putAll(profileConfigs);
        profileDAO = new ProfileConfigDAO(dataSource, tenantId);
    }

    public ProfileConfiguration getProfileConfig(String profileName) throws UserStoreException {
        if (profileName != null) {
            return profileConfigs.get(profileName);
        } else {
            throw new UserStoreException("profileName value is null.");
        }
    }

    public void addProfileConfig(org.wso2.micro.integrator.security.user.api.ProfileConfiguration profileConfig) throws UserStoreException {
        if (profileConfig != null && profileConfig.getProfileName() != null) {
            profileConfigs.put(profileConfig.getProfileName(), (ProfileConfiguration) profileConfig);
        }
        profileDAO.addProfileConfig((ProfileConfiguration) profileConfig);
    }

    public void updateProfileConfig(org.wso2.micro.integrator.security.user.api.ProfileConfiguration profileConfig) throws UserStoreException {
        if (profileConfig != null && profileConfig.getProfileName() != null) {
            profileConfigs.put(profileConfig.getProfileName(), (ProfileConfiguration) profileConfig);
        }
        profileDAO.updateProfileConfig((ProfileConfiguration) profileConfig);
    }

    public void deleteProfileConfig(org.wso2.micro.integrator.security.user.api.ProfileConfiguration profileConfig) throws UserStoreException {
        if (profileConfig != null && profileConfig.getProfileName() != null) {
            profileConfigs.remove(profileConfig.getProfileName());
        }
        profileDAO.deleteProfileConfig((ProfileConfiguration) profileConfig);
    }

    public ProfileConfiguration[] getAllProfiles() throws UserStoreException {
        return profileConfigs.values().toArray(new ProfileConfiguration[profileConfigs.size()]);
    }

}
