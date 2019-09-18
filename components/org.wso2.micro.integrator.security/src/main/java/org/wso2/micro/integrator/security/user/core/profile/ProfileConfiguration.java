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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The class managing the profile configuration. A profile of a user is
 * collection of claims. Claims can be in one of the three states - hidden,
 * overridden or inherited.
 * <ul>
 * <li>InheritedClaim - If the value of the claim is null then, read the value of the claim Default profile.</li>
 * <li>Hidden - The claim is hidden in this profile.</li>
 * <li>Overriden - Always read the value of the claim fromcurrent profile.</li>
 * </ul>
 */
public class ProfileConfiguration extends org.wso2.micro.integrator.security.user.api.ProfileConfiguration {

    /**
     * Name of the configuration.
     */
    private String profileName = null;
    /**
     * Dialect name
     */
    private String dialectName = null;
    /**
     * Hidden claim list
     */
    private List<String> hiddenClaims = new CopyOnWriteArrayList<String>();
    /**
     * Overridden claim list
     */
    private List<String> overriddenClaims = new CopyOnWriteArrayList<String>();
    /**
     * Inherited claim list
     */
    private List<String> inheritedClaims = new CopyOnWriteArrayList<String>();

    public ProfileConfiguration() {

    }

    public ProfileConfiguration(String profileName, List<String> hiddenClaims,
                                List<String> overriddenClaims, List<String> inheritedClaims) {
        super();
        this.profileName = profileName;
        this.hiddenClaims = new CopyOnWriteArrayList<String>(hiddenClaims);
        this.overriddenClaims = new CopyOnWriteArrayList<String>(overriddenClaims);
        this.inheritedClaims = new CopyOnWriteArrayList<String>(inheritedClaims);
    }

    public ProfileConfiguration(String profileName, String dialect, List<String> hiddenClaims,
                                List<String> overriddenClaims, List<String> inheritedClaims) {
        super();
        this.profileName = profileName;
        this.dialectName = dialect;
        this.hiddenClaims = new CopyOnWriteArrayList<String>(hiddenClaims);
        this.overriddenClaims = new CopyOnWriteArrayList<String>(overriddenClaims);
        this.inheritedClaims = new CopyOnWriteArrayList<String>(inheritedClaims);
    }

    public List<String> getInheritedClaims() {
        return inheritedClaims;
    }

    public void setInheritedClaims(List<String> inheritedClaims) {
        this.inheritedClaims = new CopyOnWriteArrayList<String>(inheritedClaims);
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public List<String> getHiddenClaims() {
        return hiddenClaims;
    }

    public void setHiddenClaims(List<String> hiddenClaims) {
        this.hiddenClaims = new CopyOnWriteArrayList<String>(hiddenClaims);
    }

    public List<String> getOverriddenClaims() {
        return overriddenClaims;
    }

    public void setOverriddenClaims(List<String> overriddenClaims) {
        this.overriddenClaims = new CopyOnWriteArrayList<String>(overriddenClaims);
    }

    public String getDialectName() {
        return dialectName;
    }

    public void setDialectName(String dialectName) {
        this.dialectName = dialectName;
    }

    public void addInheritedClaim(String claimUri) {
        this.inheritedClaims.add(claimUri);
    }

    public void addHiddenClaim(String claimUri) {
        this.hiddenClaims.add(claimUri);
    }

    public void addOverriddenClaim(String claimUri) {
        this.overriddenClaims.add(claimUri);
    }
}
