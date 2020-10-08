/*
Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.mi.registry.migration.utils;

public class MigrationClientUtils {
    public static final int SESSION_TIMEOUT_IN_MILLIS = 60000000;
    public static final String URL_SEPARATOR = "/";
    public static final String LOG_FILE_LOCATION_SYS_PROPERTY = "log.file.location";
    public static final String RESOURCES_FOLDER = "resources";
    public static final String ARTIFACT_XML = "artifact.xml";
    public static final String ARTIFACTS_XML = "artifacts.xml";
    public static final String REGISTRY_INFO_XML = "registry-info.xml";
    static final String ARCHIVE_EXCEPTION_MSG = "Error occurred while creating CAR file.";
    static final String EMPTY_STRING = "";
    public static final String POM_XML = "pom.xml";
    public static final String PROJECT_FILE = ".project";
    public static final String CLASSPATH_FILE = ".classpath";
    public static final String EXPORT_FAILURE_MESSAGE = "Failed to export!";
    public static final String EXPORT_SUCCESS_MESSAGE = "Successfully exported!";
    public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    public static final String[] IGNORED_REGISTRY_RESOURCES =
            {
                    "/_system/config/repository/carbonapps",
                    "/_system/config/repository/connection/props",
                    "/_system/config/repository/components/org.wso2.carbon"
                            + ".governance/configuration/services/service-schema",
                    "/_system/config/repository/components/org.wso2.carbon.governance",
                    "/_system/config/repository/components/org.wso2.carbon.mediation.throttle",
                    "/_system/config/repository/components/org.wso2.carbon.security.mgt",
                    "/_system/config/repository/components/org.wso2.carbon.logging/log4j.file.not.found",
                    "/_system/config/repository/components/org.wso2.carbon.logging/wso2carbon.system.log.last.modified",
                    "/_system/config/repository/components/org.wso2.carbon.logging/wso2carbon.system.log.pattern",
                    "/_system/config/repository/components/org.wso2.carbon.security.mgt/policy",
                    "/_system/config/repository/esb/esb-configurations/default",
                    "/_system/config/repository/esb/inbound/inbound-endpoints",
                    "/_system/config/repository/transports",
                    "/_system/governance/event/topicIndex",
                    "/_system/governance/permission",
                    "/_system/governance/repository/components/org.wso2.carbon.user.mgt",
                    "/_system/governance/repository/components/org.wso2.carbon.all-themes",
                    "/_system/governance/repository/security/key-stores/carbon-primary-ks",
                    "/_system/governance/trunk"
            };

    private MigrationClientUtils() {
    }
}
