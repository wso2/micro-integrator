/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.micro.integrator.initializer;


/**
 * Mediation initializer constants
 */
@SuppressWarnings({"UnusedDeclaration"})
public final class ServiceBusConstants {

    public static final String REGISTRY_PATH_SEPARATOR = "/";
    public static final String REGISTRY_ROOT = "/repository/";
    public static final String META_INF_REGISTRY_PATH = REGISTRY_ROOT + "esb" + REGISTRY_PATH_SEPARATOR + "configuration";
    public static final String SYNAPSE_CONFIG_REGISTRY_SPACE = REGISTRY_ROOT + "synapse";
    public static final String STRUCTURE_CREATED = "created";
    public static final String DEFAULT_COLLECTIONS_PROPERTY = "defaultCollections";
    public static final String DEFAULT_ESBREGISTRY_ITEM = "DefaultESBRegistry.Item";
    public static final String ESB_SAMPLE_SYSTEM_PROPERTY = "esb.sample";
    public static final String EI_SAMPLE_SYSTEM_PROPERTY = "ei.sample";
    public static final String USE_SYNAPE_XML_SYSTEM_PROPERTY = "useSynapseXML";
    public static final String CONFIGURATION_SERIALIZATION = "synapseConfiguration";
    public static final String SERIALIZED_TO_REGISTRY = "serializedToRegistry";
    public static final String SYNAPSE_REGISTRY_RESOURCE_NAME = "registryInfo";
    public static final String DEFINITION_FILE_NAME = "DEF_FILE_NAME";

    // XML configuration constants
    public static final String MEDIATION_CONF = "MediationConfig";
    public static final String LOAD_FROM_REGISTRY = MEDIATION_CONF + ".LoadFromRegistry";
    public static final String REGISTRY_FAIL_SAFE = MEDIATION_CONF + ".RegistryFailSafe";
    public static final String SAVE_TO_FILE = MEDIATION_CONF + ".SaveToFile";
    public static final String PERSISTENCE = MEDIATION_CONF + ".Persistence";
    public static final String REGISTRY_PERSISTENCE = MEDIATION_CONF + ".RegistryPersistence";
    public static final String WORKER_INTERVAL = MEDIATION_CONF + ".WorkerInterval";

    public static final String DISABLED = "disabled";
    public static final String ENABLED = "enabled";

    // Types of functional components
    public static final int ITEM_TYPE_PROXY_SERVICE = 0;
    public static final int ITEM_TYPE_SEQUENCE      = 1;
    public static final int ITEM_TYPE_ENDPOINT      = 2;
    public static final int ITEM_TYPE_TASK          = 3;
    public static final int ITEM_TYPE_ENTRY         = 4;
    public static final int ITEM_TYPE_EVENT_SRC     = 5;
    public static final int ITEM_TYPE_REGISTRY      = 6;
    public static final int ITEM_TYPE_FULL_CONFIG   = 7;
    public static final int ITEM_TYPE_EXECUTOR      = 8;
    public static final int ITEM_TYPE_TEMPLATE      = 9;
    public static final int ITEM_TYPE_MESSAGE_STORE =10;
    public static final int ITEM_TYPE_MESSAGE_PROCESSOR =11;
    public static final int ITEM_TYPE_TEMPLATE_ENDPOINTS = 12;
    public static final int ITEM_TYPE_REST_API      = 13;
    public static final int ITEM_TYPE_IMPORT        = 14;
    public static final int ITEM_TYPE_INBOUND       = 15;

    public static final String ARTIFACT_EXTENSION = "xml";
    public static final String CLASS_MEDIATOR_EXTENSION = "jar";
    public static final String SYNAPSE_LIBRARY_EXTENSION = "zip";
    public static final String PERSISTENCE_MANAGER = "PERSISTENCE_MANAGER";
    public static final String SYNAPSE_CURRENT_CONFIGURATION = "SYNAPSE_CURRENT_CONFIGURATION";
    public static final String CARBON_TASK_SCHEDULER = "CARBON_TASK_SCHEDULER";
    public static final String CARBON_TASK_REPOSITORY = "CARBON_TASK_REPOSITORY";
    public static final String DEFAULT_SYNAPSE_CONFIGS_LOCATION = "repository/deployment/server/synapse-configs";
    public static final String SYNAPSE_CONFIGS = "synapse-configs";
    public static final String SYNAPSE_LIB_CONFIGS = "synapse-libs";
    public static final String SYNAPSE_CONFIG_LOCK = "synapse.config.lock";

    public static final String SEQUENCE_TYPE = "synapse/sequence";
    public static final String ENDPOINT_TYPE = "synapse/endpoint";
    public static final String PROXY_SERVICE_TYPE = "synapse/proxy-service";
    public static final String LOCAL_ENTRY_TYPE = "synapse/local-entry";
    public static final String EVENT_SOURCE_TYPE = "synapse/event-source";
    public static final String TASK_TYPE = "synapse/task";
    public static final String MESSAGE_STORE_TYPE = "synapse/message-store";
    public static final String MESSAGE_PROCESSOR_TYPE = "synapse/message-processors";

    public static final class RegistryStore {
        public static final String SEQUENCE_REGISTRY = "sequences";
        public static final String TEMPLATE_REGISTRY = "templates";
        public static final String ENDPOINT_REGISTRY = "endpoints";
        public static final String PROXY_SERVICE_REGISTRY = "proxy-services";
        public static final String EVENT_SOURCE_REGISTRY = "event-sources";
        public static final String LOCAL_ENTRY_REGISTRY = "local-entries";
        public static final String SYNAPSE_STARTUP_REGISTRY = "synapse-startups";
        public static final String SYNAPSE_REGISTRY_REGISTRY = "synapse-registry";
        public static final String EXECUTOR_REGISTRY = "synapse-executors";
        public static final String MESSAGE_STORE_REGISTRY = "synapse-message-stores";
        public static final String MESSAGE_PROCESSOR_REGISTRY = "synapse-message-processors";
        public static final String REST_API_REGISTRY = "api";
        public static final String IMPORT_REGISTRY = "imports";
    }

    // String constants used for storing information about synapse configurations
    public static final String ESB_CONFIGURATIONS = "repository/esb/esb-configurations";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String ACTIVE = "ACTIVE";
    public static final String CREATED = "CREATED";

    //constant for registry based wsdl dependency
    public static final String DEPENDS = "depends";

    public static final String SUSPEND_PERSISTENCE = "suspend.mediation.persistence";
    public static final String CONNECTOR_SECURE_VAULT_CONFIG_REPOSITORY = "/repository/components/secure-vault";


    public static final String ESB_DEBUG_SYSTEM_PROPERTY = "esb.debug";
    public static final String ESB_DEBUG_EVENT_PORT = "synapse.debugger.port.event";
    public static final String ESB_DEBUG_COMMAND_PORT = "synapse.debugger.port.command";

    // Constant holding synapse import config path from axis2 repository root
    public static final String SYNAPSE_IMPORTS_CONFIG_PATH = "synapse-configs/default/imports";
    public static final String SYNAPSE_CONNECTOR_PACKAGE = "org.wso2.carbon.connector";
    public static final String DISABLE_CONNECTOR_INIT_SYSTEM_PROPERTY = "esb.connector.startup.init.disable";

}

