/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.micro.integrator.initializer.utils;

/**
 * This class contains the Constant values used in the service catalog feature.
 */
public class Constants {

    public static final String MI_HOST = "MI_HOST";
    public static final String MI_PORT = "MI_PORT";
    public static final String MI_URL = "MI_URL";
    public static final String HOST = "{host}";
    public static final String PORT = "{port}";
    public static final String URL = "{url}";
    public static final String SERVICE_URL = "serviceUrl";
    public static final String SERVICE_KEY = "serviceKey";
    public static final String METADATA_KEY = "key";
    public static final String MD5 = "md5";
    public static final String VERIFIER = "verifier";
    public static final String LIST_STRING = "list";

    // deployment.toml related constants
    public static final String SERVICE_CATALOG_CONFIG = "service_catalog";
    public static final String APIM_HOST = "apimHost";
    public static final String ENABLE = "enable";
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";

    public static final String SERVICE_CATALOG_PUBLISH_ENDPOINT = "api/am/service-catalog/v0/services/import?overwrite" +
            "=true";
    public static final String SERVICE_CATALOG_GET_SERVICES_ENDPOINT = "api/am/service-catalog/v0/services";

    // creating the payload.zip related constants
    public static final String SERVICE_CATALOG = "ServiceCatalog";
    public static final String CAPP_FOLDER_NAME = "carbonapps";
    public static final String METADATA_FOLDER_STRING = "_metadata_";
    public static final String METADATA_FILE_STRING = "_metadata-";
    public static final String SWAGGER_FOLDER_STRING = "_swagger_";
    public static final String SWAGGER_FILE_STRING = "_swagger-";
    public static final String METADATA_FOLDER_NAME = "metadata";
    public static final String TEMP_FOLDER_NAME = "temp";
    public static final String ZIP_FOLDER_NAME = "payload.zip";
    public static final String METADATA_FILE_NAME = "metadata.yaml";
    public static final String SWAGGER_FILE_NAME = "definition.yaml";
    public static final String YAML_FILE_EXTENSION = ".yaml";

    // constants related to retry mechanism
    public static final int INTERVAL_BETWEEN_RETRIES = 2000;
    public static final int RETRY_COUNT = 3;

    public static final int UNAUTHENTICATED = 401;

}
