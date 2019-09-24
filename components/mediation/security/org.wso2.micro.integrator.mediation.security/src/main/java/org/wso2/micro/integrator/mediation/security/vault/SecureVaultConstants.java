package org.wso2.carbon.mediation.security.vault;

public interface SecureVaultConstants {
	public static final String SYSTEM_CONFIG_CONNECTOR_SECURE_VAULT_CONFIG =
	                                                                         "/_system/config/repository/components/secure-vault";
	public static final String CONNECTOR_SECURE_VAULT_CONFIG_REPOSITORY =
	                                                                      "conf:/repository/components/secure-vault";
	public static final String CARBON_HOME = "carbon.home";
	public static final String SECRET_CONF = "secret-conf.properties";
	public static final String CONF_LOCATION = "conf.location";
	public static final String CONF_DIR = "conf";
	public static final String REPOSITORY_DIR = "repository";
	public static final String SECURITY_DIR = "security";
	/* Default configuration file path for secret manager */
	public final static String PROP_DEFAULT_CONF_LOCATION = "secret-manager.properties";
	/*
	 * If the location of the secret manager configuration is provided as a
	 * property- it's name
	 */
	public final static String PROP_SECRET_MANAGER_CONF = "secret.manager.conf";
	/* Property key for secretRepositories */
	public final static String PROP_SECRET_REPOSITORIES = "secretRepositories";
	/* Type of the secret repository */
	public final static String PROP_PROVIDER = "provider";
	/* Dot string */
	public final static String DOT = ".";

	// property key for global secret provider
	public final static String PROP_SECRET_PROVIDER = "carbon.secretProvider";

	public final static String SERVELT_SESSION = "comp.mgt.servlet.session";

	public static final String CONF_CONNECTOR_SECURE_VAULT_CONFIG_PROP_LOOK =
	                                                                          "conf:/repository/components/secure-vault";

}
