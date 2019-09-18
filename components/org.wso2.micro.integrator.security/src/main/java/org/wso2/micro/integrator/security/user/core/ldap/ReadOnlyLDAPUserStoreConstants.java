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
package org.wso2.micro.integrator.security.user.core.ldap;


import org.wso2.micro.integrator.security.user.api.Property;
import org.wso2.micro.integrator.security.user.core.UserStoreConfigConstants;

import java.util.ArrayList;

public class ReadOnlyLDAPUserStoreConstants {


    //Properties for Read Write LDAP User Store Manager
    public static final ArrayList<Property> ROLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> OPTIONAL_ROLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();

    //For multiple attribute separation
    private static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    private static final String MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION = "This is the separator for multiple claim values";
    private static final String DisplayNameAttributeDescription = "Attribute name to display as the Display Name";
    private static final String DisplayNameAttribute = "DisplayNameAttribute";
    private static final String roleDNPattern = "RoleDNPattern";
    private static final String roleDNPatternDescription = "The patten for role's DN. It can be defined to improve " +
            "the LDAP search";


    static {

        setMandatoryProperty(UserStoreConfigConstants.connectionURL, "Connection URL", "ldap://",
                UserStoreConfigConstants.connectionURLDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.connectionName, "Connection Name", "uid=," +
                "ou=", UserStoreConfigConstants.connectionNameDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.connectionPassword, "Connection Password",
                "", UserStoreConfigConstants.connectionPasswordDescription, true);
        setMandatoryProperty(UserStoreConfigConstants.userSearchBase, "User Search Base",
                "ou=system", UserStoreConfigConstants.userSearchBaseDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.userNameAttribute, "Username Attribute",
                "", UserStoreConfigConstants.userNameAttributeDescription, false);

        setMandatoryProperty(UserStoreConfigConstants.usernameSearchFilter, "User Search Filter",
                "(&(objectClass=person)(uid=?))", UserStoreConfigConstants
                        .usernameSearchFilterDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.usernameListFilter, "User List Filter",
                "(objectClass=person)", UserStoreConfigConstants.usernameListFilterDescription, false);


        setProperty(UserStoreConfigConstants.userDNPattern, "User DN Pattern", "", UserStoreConfigConstants.userDNPatternDescription);
        setProperty(DisplayNameAttribute, "Display name attribute", "", DisplayNameAttributeDescription);
        setProperty(UserStoreConfigConstants.disabled, "Disabled", "false", UserStoreConfigConstants.disabledDescription);
        setProperty(UserStoreConfigConstants.readGroups, "Read Groups", "true", UserStoreConfigConstants
                .readLDAPGroupsDescription);
        setProperty(UserStoreConfigConstants.groupSearchBase, "Group Search Base", "ou=Groups,dc=wso2,dc=org",
                UserStoreConfigConstants.groupSearchBaseDescription);
        setProperty(UserStoreConfigConstants.groupNameAttribute, "Group Name Attribute", "cn", UserStoreConfigConstants.groupNameAttributeDescription);
        setProperty(UserStoreConfigConstants.groupNameSearchFilter, "Group Search Filter",
                "(&(objectClass=groupOfNames)(cn=?))", UserStoreConfigConstants.groupNameSearchFilterDescription);
        setProperty(UserStoreConfigConstants.groupNameListFilter, "Group List Filter", "(objectClass=groupOfNames)",
                UserStoreConfigConstants.groupNameListFilterDescription);

        setProperty(roleDNPattern, "Role DN Pattern", "", roleDNPatternDescription);

        setProperty(UserStoreConfigConstants.membershipAttribute, "Membership Attribute", "member", UserStoreConfigConstants.membershipAttributeDescription);
        setProperty(UserStoreConfigConstants.memberOfAttribute, "Member Of Attribute", "", UserStoreConfigConstants.memberOfAttribute);
        setProperty("BackLinksEnabled", "Enable Back Links", "false", " Whether to allow attributes to be result from" +
                "references to the object from other objects");

        setProperty("Referral", "Referral", "follow", "Guides the requests to a domain controller in the correct domain");
        setProperty("ReplaceEscapeCharactersAtUserLogin", "Enable Escape Characters at User Login", "true", "Whether replace escape character when user login");
        setProperty("UniqueID", "", "", "");
        setProperty(UserStoreConfigConstants.lDAPInitialContextFactory, "LDAP Initial Context Factory",
                "com.sun.jndi.ldap.LdapCtxFactory", UserStoreConfigConstants.lDAPInitialContextFactoryDescription);
    }

    private static void setMandatoryProperty(String name, String displayName, String value,
                                             String description, boolean encrypt) {
        String propertyDescription = displayName + "#" + description;
        if (encrypt) {
            propertyDescription += "#encrypt";
        }
        Property property = new Property(name, value, propertyDescription, null);
        ROLDAP_USERSTORE_PROPERTIES.add(property);

    }

    private static void setProperty(String name, String displayName, String value,
                                    String description) {
        Property property = new Property(name, value, displayName + "#" + description, null);
        OPTIONAL_ROLDAP_USERSTORE_PROPERTIES.add(property);

    }


}
