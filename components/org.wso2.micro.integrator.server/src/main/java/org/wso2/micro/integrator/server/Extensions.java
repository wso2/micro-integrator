/*
 *Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *WSO2 LLC. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */
package org.wso2.micro.integrator.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.config.mapper.ConfigParser;
import org.wso2.config.mapper.ConfigParserException;
import org.wso2.micro.integrator.server.extensions.*;

import java.nio.file.Paths;

public class Extensions {
    private static Log logger = LogFactory.getLog(Extensions.class);

    public static void main(String[] args) {
        logger.info("Running extension runner...");
        handleConfiguration();

        new DefaultBundleCreator().perform();
        new SystemBundleExtensionCreator().perform();
        //copying patched jars to components/plugins dir
        new PatchInstaller().perform();
        new LibraryFragmentBundleCreator().perform();

        //Add bundles in the dropins directory to the bundles.info file.
        new DropinsBundleDeployer().perform();

        //rewriting the eclipse.ini file
        new EclipseIniRewriter().perform();
        logger.info("Extension Runner completed.");
    }

    private static void handleConfiguration() {

        String resourcesDir = Paths.get(System.getProperty(LauncherConstants.CARBON_HOME),
                "repository", "resources", "conf").toString();
        String configFilePath = Paths.get(System.getProperty(LauncherConstants.CARBON_CONFIG_DIR_PATH),
                ConfigParser.UX_FILE_PATH).toString();
        String outputDir = System.getProperty(LauncherConstants.CARBON_HOME);
        try {
            ConfigParser.parse(configFilePath, resourcesDir, outputDir);
        } catch (ConfigParserException e) {
            logger.fatal("Error while performing configuration changes", e);
            System.exit(1);
        }
    }
}
