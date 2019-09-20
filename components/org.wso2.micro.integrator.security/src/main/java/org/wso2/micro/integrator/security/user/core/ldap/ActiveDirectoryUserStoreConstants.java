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

public class ActiveDirectoryUserStoreConstants {


    //Properties for Read Active Directory User Store Manager
    public static final ArrayList<Property> ACTIVE_DIRECTORY_UM_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> OPTIONAL_ACTIVE_DIRECTORY_UM_PROPERTIES = new ArrayList<Property>();

    //For multiple attribute separation
    private static final String DisplayNameAttributeDescription = "Attribute name to display as the Display Name";
    private static final String DisplayNameAttribute = "DisplayNameAttribute";
    private static final String usernameJavaRegExViolationErrorMsg = "UsernameJavaRegExViolationErrorMsg";
    private static final String usernameJavaRegExViolationErrorMsgDescription = "Error message when the Username is not " +
            "matched with UsernameJavaRegEx";
    private static final String passwordJavaRegEx = "PasswordJavaRegEx";
    private static final String passwordJavaRegExViolationErrorMsg = "PasswordJavaRegExViolationErrorMsg";
    private static final String passwordJavaRegExViolationErrorMsgDescription = "Error message when the Password is " +
            "not matched with passwordJavaRegEx";
    private static final String passwordJavaRegExDescription = "Policy that defines the password format in backend";
    private static final String roleDNPattern = "RoleDNPattern";
    private static final String roleDNPatternDescription = "The patten for role's DN. It can be defined to improve " +
            "the LDAP search";

    public static final String TRANSFORM_OBJECTGUID_TO_UUID = "transformObjectGUIDToUUID";
    public static final String TRANSFORM_OBJECTGUID_TO_UUID_DESC = "Return objectGUID in UUID Canonical Format";

    static {
        //Set mandatory properties
        setMandatoryProperty(UserStoreConfigConstants.connectionURL, "Connection URL",
                "ldaps://", UserStoreConfigConstants.connectionURLDescription, false);

        setMandatoryProperty(UserStoreConfigConstants.connectionName, "Connection Name", "CN=," +
                "DC=", UserStoreConfigConstants.connectionNameDescription, false);

        setMandatoryProperty(UserStoreConfigConstants.connectionPassword, "Connection Password",
                "", UserStoreConfigConstants.connectionPasswordDescription, true);

        setMandatoryProperty(UserStoreConfigConstants.userSearchBase, "User Search Base",
                "CN=Users,DC=WSO2,DC=Com", UserStoreConfigConstants.userSearchBaseDescription, false);

        setMandatoryProperty(UserStoreConfigConstants.userEntryObjectClass, "User Entry Object Class", "user",
                UserStoreConfigConstants.userEntryObjectClassDescription, false);

        setMandatoryProperty(UserStoreConfigConstants.userNameAttribute, "Username Attribute",
                "cn", UserStoreConfigConstants.userNameAttributeDescription, false);

        setMandatoryProperty(UserStoreConfigConstants.usernameSearchFilter, "User Search Filter",
                "(&(objectClass=user)(cn=?))", UserStoreConfigConstants
                        .usernameSearchFilterDescription, false);

        setMandatoryProperty(UserStoreConfigConstants.usernameListFilter, "User List Filter",
                "(objectClass=person)", UserStoreConfigConstants.usernameListFilterDescription, false);


        //Set optional properties

        setProperty(UserStoreConfigConstants.userDNPattern, "User DN Pattern", "",
                UserStoreConfigConstants.userDNPatternDescription);
        setProperty(DisplayNameAttribute, "Display name attribute", "", DisplayNameAttributeDescription);

        setProperty(UserStoreConfigConstants.disabled, "Disabled", "false", UserStoreConfigConstants.disabledDescription);

        Property readLDAPGroups = new Property(UserStoreConfigConstants.readGroups, "true", "Read Groups#" + UserStoreConfigConstants.readLDAPGroupsDescription, null);
        //Mandatory only if readGroups is enabled
        Property groupSearchBase = new Property(UserStoreConfigConstants.groupSearchBase, "CN=Users,DC=WSO2,DC=Com",
                "Group Search Base#" + UserStoreConfigConstants.groupSearchBaseDescription, null);
        Property groupNameListFilter = new Property(UserStoreConfigConstants.groupNameListFilter, "(objectcategory=group)",
                "Group Filter#" + UserStoreConfigConstants.groupNameListFilterDescription, null);
        Property groupNameAttribute = new Property(UserStoreConfigConstants.groupNameAttribute, "cn", "Group Name Attribute#"
                + UserStoreConfigConstants.groupNameAttributeDescription, null);
        Property membershipAttribute = new Property(UserStoreConfigConstants.membershipAttribute, "member",
                "Membership Attribute#" + UserStoreConfigConstants.membershipAttributeDescription, null);
        Property groupNameSearchFilter = new Property(UserStoreConfigConstants.groupNameSearchFilter,
                "(&(objectClass=group)(cn=?))", "Group Search Filter#" + UserStoreConfigConstants
                .groupNameSearchFilterDescription, null);
        readLDAPGroups.setChildProperties(new Property[]{groupSearchBase, groupNameAttribute, groupNameListFilter,
                membershipAttribute, groupNameSearchFilter});
        OPTIONAL_ACTIVE_DIRECTORY_UM_PROPERTIES.add(readLDAPGroups);

        setProperty(UserStoreConfigConstants.writeGroups, "Write Groups", "true", UserStoreConfigConstants.writeGroupsDescription);
        setProperty(UserStoreConfigConstants.groupSearchBase, "Group Search Base", "CN=Users,DC=WSO2,DC=Com",
                UserStoreConfigConstants.groupSearchBaseDescription);

        setProperty(UserStoreConfigConstants.groupEntryObjectClass, "Group Entry Object Class", "group",
                UserStoreConfigConstants.groupEntryObjectClassDescription);
        setProperty(UserStoreConfigConstants.groupNameAttribute, "Group Name Attribute", "cn",
                UserStoreConfigConstants.groupNameAttributeDescription);
        setProperty(UserStoreConfigConstants.groupNameSearchFilter, "Group Search Filter", "(&(objectClass=group)(cn=?))",
                UserStoreConfigConstants.groupNameSearchFilterDescription);
        setProperty(UserStoreConfigConstants.groupNameListFilter, "Group List Filter", "(objectcategory=group)",
                UserStoreConfigConstants.groupNameListFilterDescription);

        setProperty(roleDNPattern, "Role DN Pattern", "", roleDNPatternDescription);

        setProperty(UserStoreConfigConstants.membershipAttribute, "Membership Attribute", "member",
                UserStoreConfigConstants.membershipAttributeDescription);
        setProperty(UserStoreConfigConstants.memberOfAttribute, "Member Of Attribute", "memberOf",
                UserStoreConfigConstants.memberOfAttribute);
        setProperty("BackLinksEnabled", "Enable Back Links", "true", " Whether to allow attributes to be result" +
                " from references to the object from other objects");
        setProperty("Referral", "Referral", "follow", "Guides the requests to a domain controller in the correct domain");

        setProperty(UserStoreConfigConstants.usernameJavaRegEx, "Username RegEx (Java)", "[a-zA-Z0-9._-|//]{3,30}$",
                UserStoreConfigConstants.usernameJavaRegExDescription);
        setProperty(UserStoreConfigConstants.usernameJavaScriptRegEx, "Username RegEx (Javascript)", "^[\\S]{3,30}$",
                UserStoreConfigConstants.usernameJavaScriptRegExDescription);

        setProperty(usernameJavaRegExViolationErrorMsg, "Username RegEx Violation Error Message",
                "Username pattern policy violated.", usernameJavaRegExViolationErrorMsgDescription);

        setProperty(passwordJavaRegEx, "Password RegEx (Java)", "^[\\S]{5,30}$", passwordJavaRegExDescription);
        setProperty(UserStoreConfigConstants.passwordJavaScriptRegEx, "Password RegEx (Javascript)", "^[\\S]{5,30}$",
                UserStoreConfigConstants.passwordJavaScriptRegExDescription);

        setProperty(passwordJavaRegExViolationErrorMsg, "Password RegEx Violation Error Message",
                "Password pattern policy violated.", passwordJavaRegExViolationErrorMsgDescription);

        setProperty(UserStoreConfigConstants.roleNameJavaRegEx, "Role Name RegEx (Java)", "[a-zA-Z0-9._-|//]{3,30}$",
                UserStoreConfigConstants.roleNameJavaRegExDescription);

        setProperty(UserStoreConfigConstants.roleNameJavaScriptRegEx, "Role Name RegEx (Javascript)", "^[\\S]{3,30}$",
                UserStoreConfigConstants.roleNameJavaScriptRegExDescription);
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
        ACTIVE_DIRECTORY_UM_PROPERTIES.add(property);

    }

    private static void setProperty(String name, String displayName, String value,
                                    String description) {
        Property property = new Property(name, value, displayName + "#" + description, null);
        OPTIONAL_ACTIVE_DIRECTORY_UM_PROPERTIES.add(property);

    }


}
