/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.micro.integrator.dataservices.sql.driver.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gdata.client.Query;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.IEntry;
import com.google.gdata.data.IFeed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.integrator.dataservices.sql.driver.parser.Constants;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.SQLException;

/**
 * Helper class to manipulate feed requests with access tokens
 */
public class GSpreadFeedProcessor {

    private static final Log log = LogFactory.getLog(GSpreadFeedProcessor.class);

    private String clientId;

    private String clientSecret;

    private String accessToken;

    private String refreshToken;

    private String visibility = Constants.ACCESS_MODE_PRIVATE;

    private SpreadsheetService service;

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setService(SpreadsheetService service) {
        this.service = service;
    }

    public SpreadsheetService getService() {
        return service;
    }

    private String charSetType = "UTF-8";

    private String baseRegistryOauthTokenPath = "/repository/components/org.wso2.carbon.dataservices.sql.driver/tokens/";

    public GSpreadFeedProcessor(String clientId, String clientSecret, String refReshToken,
                                String visibility, String baseRegistryOauthTokenPath) throws SQLException {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refReshToken;
        this.visibility = visibility;
        if (!this.checkVisibility()) {
            throw new SQLException("Invalid access mode '" + visibility + "' is provided");
        }
        if (requiresAuth()) {
            if (this.clientId == null || this.clientId.isEmpty()){
                throw new SQLException("Valid Client id not provided");
            }
            if (this.clientSecret == null || this.clientSecret.isEmpty()){
                throw new SQLException("Valid Client secret not provided");
            }
            if (this.refreshToken == null || this.refreshToken.isEmpty()){
                throw new SQLException("Valid refresh token not provided");
            }
            try {
                this.clientId = URLDecoder.decode(this.clientId, "UTF-8");
                this.clientSecret = URLDecoder.decode(this.clientSecret, "UTF-8");
                this.refreshToken = URLDecoder.decode(this.refreshToken, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new SQLException("Error in retrieving Authentication information " + e.getMessage(), e);
            }
        }
        this.baseRegistryOauthTokenPath = baseRegistryOauthTokenPath;
    }


    public <E extends IEntry> E insert(URL feedUrl, E entry) throws SQLException {
        try {
            if (this.requiresAuth()) {
                if (this.accessToken != null) {
                    this.authenticateWithAccessToken();
                    try {
                        return this.service.insert(feedUrl, entry);
                    } catch (Exception e) {
                        log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with current AccessToken ", e);
                    }
//                    String accessTokenFromRegistry = this.getAccessTokenFromRegistry();
//                    if (accessTokenFromRegistry != null && this.accessToken != accessTokenFromRegistry) {
//                        this.accessToken = accessTokenFromRegistry;
//                        this.authenticateWithAccessToken();
//                        try {
//                            return this.service.insert(feedUrl, entry);
//                        } catch (Exception e) {
//                            log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with AccessToken from registry ", e);
//                        }
//                    }
                }
//                this.refreshAndAuthenticate();
//                this.saveTokenToRegistry();
            }
            return this.service.insert(feedUrl, entry);
        } catch (Exception e) {
            throw new SQLException("Error in retrieving Feed data " + e.getMessage(), e);
        }
    }

    /**
     * this method has the logic implemented to use access token to access spreadsheet api
     * and it will be shared between cluster nodes via registry as well. this will refresh the access token
     * if access tokens stored in memory and registry are expired, then it will store the new access token
     * in registry so that it will be shared among nodes
     *
     * @param feedUrl
     * @param feedClass
     * @param <F>
     * @return feed
     * @throws Exception
     */
    public <F extends IFeed> F getFeed(URL feedUrl, Class<F> feedClass) throws SQLException {
        try {
            if (this.requiresAuth()) {
                if (this.accessToken != null) {
                    this.authenticateWithAccessToken();
                    try {
                        return this.service.getFeed(feedUrl, feedClass);
                    } catch (Exception e) {
                        log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with current AccessToken ", e);
                    }
//                    String accessTokenFromRegistry = this.getAccessTokenFromRegistry();
//                    if (accessTokenFromRegistry != null && !this.accessToken.equals(accessTokenFromRegistry)) {
//                        this.accessToken = accessTokenFromRegistry;
//                        this.authenticateWithAccessToken();
//                        try {
//                            return this.service.getFeed(feedUrl, feedClass);
//                        } catch (Exception e) {
//                            log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with AccessToken from registry ", e);
//                        }
//                    }
                }
//                this.refreshAndAuthenticate();
//                this.saveTokenToRegistry();
            }
            return this.service.getFeed(feedUrl, feedClass);
        } catch (Exception e) {
            throw new SQLException("Error in retrieving Feed data " + e.getMessage(), e);
        }
    }

    /**
     * this method has the logic implemented to use access token to access spreadsheet api
     * and it will be shared between cluster nodes via registry as well. this will refresh the access token
     * if access tokens stored in memory and registry are expired, then it will store the new access token
     * in registry so that it will be shared among nodes
     *
     * @param query
     * @param feedClass
     * @param <F>
     * @return feed
     * @throws Exception
     */
    public <F extends IFeed> F getFeed(Query query, Class<F> feedClass) throws SQLException {
        try {
            if (this.requiresAuth()) {
                if (this.accessToken != null) {
                    this.authenticateWithAccessToken();
                    try {
                        return this.service.getFeed(query, feedClass);
                    } catch (Exception e) {
                        log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with current AccessToken ", e);
                    }
//                    String accessTokenFromRegistry = this.getAccessTokenFromRegistry();
//                    if (accessTokenFromRegistry != null && !this.accessToken.equals(accessTokenFromRegistry)) {
//                        this.accessToken = accessTokenFromRegistry;
//                        this.authenticateWithAccessToken();
//                        try {
//                            return this.service.getFeed(query, feedClass);
//                        } catch (Exception e) {
//                            log.warn("GSpreadConfig.getFeed(): Failed to retrieve Feeds with AccessToken from registry ", e);
//                        }
//                    }
                }
//                this.refreshAndAuthenticate();
//                this.saveTokenToRegistry();
            }
            return this.service.getFeed(query, feedClass);
        } catch (Exception e) {
            throw new SQLException("Error in retrieving Feed data " + e.getMessage(), e);
        }
    }

    /**
     * helper method to authenticate using just access token
     */
    private void authenticateWithAccessToken() {
        GoogleCredential credential = getBaseCredential();
        credential.setAccessToken(this.accessToken);
        this.service.setOAuth2Credentials(credential);
    }

    /**
     * helper method to refresh the access token and authenticate
     *
     * @throws Exception
     */
    private void refreshAndAuthenticate() throws Exception {
        GoogleCredential credential = getBaseCredential();
        credential.setAccessToken(this.accessToken);
        credential.setRefreshToken(this.refreshToken);
        credential.refreshToken();
        this.accessToken = credential.getAccessToken();
        this.service.setOAuth2Credentials(credential);
    }

    /**
     * helper method to get the base credential object
     *
     * @return credential
     */
    private GoogleCredential getBaseCredential() {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(this.clientId, this.clientSecret)
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .build();
        return credential;
    }

    private String generateAuthTokenResourcePath() {
//		StringBuilder userKey = new StringBuilder();
//		/* append the username value 3 times because,
//		 * later when we do base64 encoding, we have to be sure,
//		 * it doesn't have "=" characters by making the source data
//		 * a multiple of 3, thus not to have any padding data.
//		 */
        String resPath = this.baseRegistryOauthTokenPath
                + "configs/"
                + "user_auth_token/users/"
                + this.clientId;
        return resPath;
    }

    /**
     * Helper method to get current access token resides in the registry.
     *
     * @return accessToken
     * @throws Exception
     */
//    private String getAccessTokenFromRegistry() throws Exception {
//        if (SQLDriverDSComponent.getRegistryService() == null) {
//            String msg = "GSpreadConfig.getFeed(): Registry service is not available, authentication key sharing fails";
//            throw new SQLException(msg);
//        }
//        Registry registry = SQLDriverDSComponent.getRegistryService()
//                        .getGovernanceSystemRegistry(TDriverUtil.getCurrentTenantId());
//        Resource authTokenRes = this.getAuthTokenResource(registry);
//        if (authTokenRes != null) {
//            Object content = authTokenRes.getContent();
//            if (content != null) {
//                return new String((byte[]) content, this.charSetType);
//            }
//        }
//        return null;
//    }

    /**
     * Helper method to save new access token to registry.
     *
     * @throws Exception
     */
//    private void saveTokenToRegistry() throws Exception {
//        if (SQLDriverDSComponent.getRegistryService() == null) {
//            String msg = "GSpreadConfig.getFeed(): Registry service is not available, authentication key cannot be" +
//                         " saved";
//            throw new SQLException(msg);
//        }
//        Registry registry = SQLDriverDSComponent.getRegistryService()
//                .getGovernanceSystemRegistry(TDriverUtil.getCurrentTenantId());
//        registry.beginTransaction();
//        Resource res = registry.newResource();
//        res.setContent(this.accessToken.getBytes(this.charSetType));
//        registry.put(this.generateAuthTokenResourcePath(), res);
//        registry.commitTransaction();
//    }


    public URL getSpreadSheetFeedUrl() throws MalformedURLException {
        return new URL(Constants.SPREADSHEET_FEED_BASE_URL + this.visibility + "/full");
    }

    public URL generateWorksheetFeedURL(String key) throws MalformedURLException {
        return new URL(Constants.BASE_WORKSHEET_URL + key + "/" +
                               this.visibility + "/basic");
    }

    /**
     * method to check whether authentication is required or not
     *
     * @return true if authentication is required else false
     */
    public boolean requiresAuth() {
        return (this.visibility != null &&
                this.visibility.equals(Constants.ACCESS_MODE_PRIVATE));
    }

    private boolean checkVisibility() {
        return (Constants.ACCESS_MODE_PRIVATE.equals(this.visibility) ||
                Constants.ACCESS_MODE_PUBLIC.equals(this.visibility));
    }
}
