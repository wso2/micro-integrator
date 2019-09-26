/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.management.apis;

public class Constants {
    //Constants for the API resource prefixes
    public static final String REST_API_CONTEXT = "/management";
    public static final String PREFIX_APIS = "/apis";
    public static final String PREFIX_CARBON_APPS = "/applications";
    public static final String PREFIX_ENDPOINTS = "/endpoints";
    public static final String PREFIX_INBOUND_ENDPOINTS = "/inbound-endpoints";
    public static final String PREFIX_PROXY_SERVICES = "/proxy-services";
    public static final String PREFIX_TASKS = "/tasks";
    public static final String PREFIX_SEQUENCES = "/sequences";
    public static final String PREFIX_LOGGING = "/logging";
    public static final String PREFIX_DATA_SERVICES = "/data-services";
    public static final String PREFIX_TEMPLATES = "/templates";
    public static final String PREFIX_MESSAGE_STORE = "/message-stores";
    public static final String PREFIX_MESSAGE_PROCESSORS = "/message-processors";
    public static final String PREFIX_LOCAL_ENTRIES = "/local-entries";
    public static final String PREFIX_CONNECTORS = "/connectors";
    public static final String PREFIX_LOGIN = "/login";
    public static final String PREFIX_LOGOUT = "/logout";
    public static final String PREFIX_SERVER_DATA = "/server";

    public static final String COUNT = "count";
    public static final String LIST = "list";
    public static final String NAME = "name";
    public static final String STATUS = "status";
    public static final String URL = "url";
    public static final String VERSION = "version";
    public static final String CONTAINER = "container";
    public static final String TYPE = "type";
    public static final String METHOD = "method";
    public static final String STATS = "stats";
    public static final String TRACING = "tracing";
    public static final String ENABLED = "enabled";
    public static final String DISABLED = "disabled";
    public static final String MESSAGE = "message";
    public static final String LEVEL = "level";
    public static final String PARENT = "parent";
    public static final String LOGGER_NAME = "loggerName";
    public static final String LOGGING_LEVEL = "loggingLevel";
    public static final String ROOT_LOGGER = "root";

    public static final String NO_ENTITY_BODY = "NO_ENTITY_BODY";
    public static final String HTTP_STATUS_CODE = "HTTP_SC";
    public static final String HTTP_METHOD_PATCH = "PATCH";
    public static final String NOT_FOUND = "404";
    public static final String INTERNAL_SERVER_ERROR = "500";
    public static final String BAD_REQUEST = "400";

    public static final String HTTP_METHOD_PROPERTY = "HTTP_METHOD";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";

    public static final String HEADER_VALUE_APPLICATION_JSON = "application/json";
    public static final String MESSAGE_JSON_ATTRIBUTE = "Message";

    // Json attribute in response for synapse configuration
    public static final String SYNAPSE_CONFIGURATION = "configuration";

    // Types of functional components
    public static final int ITEM_TYPE_IMPORT = 14;

    // Synapse service statuses
    public static final String ACTIVE_STATUS = "active";
    public static final String INACTIVE_STATUS = "inactive";
}
