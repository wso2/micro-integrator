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

package org.wso2.micro.integrator.mediation.security.vault.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.mediation.security.vault.SecureVaultConstants;

public class SecureVaultUtil {

	private SecureVaultUtil(){
		//private constructor to avoid instance creation
	}

	private static Log log = LogFactory.getLog(SecureVaultUtil.class);

	public static Properties loadProperties() {
		Properties properties = new Properties();
		String confPath = System.getProperty(SecureVaultConstants.CONF_LOCATION);
		if (confPath == null) {
			confPath = Paths.get("repository", "conf").toString();
		}
		String filePath = Paths.get(confPath, SecureVaultConstants.SECURITY_DIR, SecureVaultConstants.SECRET_CONF).toString();

		File dataSourceFile = new File(filePath);
		if (!dataSourceFile.exists()) {
			return properties;
		}

		try (InputStream in = new FileInputStream(dataSourceFile)) {
			properties.load(in);
		} catch (IOException e) {
			String msg = "Error loading properties from a file at :" + filePath;
			log.warn(msg, e);
			return properties;
		}
		return properties;
	}

}
