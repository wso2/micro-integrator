package org.wso2.carbon.mediation.security.vault.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mediation.security.vault.SecureVaultConstants;

public class SecureVaultUtil {

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

		InputStream in = null;
		try {
			in = new FileInputStream(dataSourceFile);
			properties.load(in);
		} catch (IOException e) {
			String msg = "Error loading properties from a file at :" + filePath;
			log.warn(msg, e);
			return properties;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignored) {

				}
			}
		}
		return properties;
	}

}
