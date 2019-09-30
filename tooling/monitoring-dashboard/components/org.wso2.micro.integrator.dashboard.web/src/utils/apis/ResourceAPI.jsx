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


import Axios from 'axios';

const https = require('https');
import AuthManager from '../../auth/utils/AuthManager';
import {Constants} from '../../auth/Constants';

const baseURL = `https://${window.localStorage.getItem('host')}:${window.localStorage.getItem('port')}/management`;


export default class ResourceAPI {

    getHTTPClient() {
        let httpClient = Axios.create({
            baseURL: baseURL,
            timeout: 30000,
            httpsAgent: new https.Agent({
                rejectUnauthorized: false
            }),
            headers: {"Authorization": "Bearer " + AuthManager.getCookie(Constants.JWT_TOKEN_COOKIE)}
        });
        httpClient.defaults.headers.post['Content-Type'] = 'application/json';
        httpClient.interceptors.response.use(function (response) {
            return response;
        }, function (error) {
            if (401 === error.response.status) {
                AuthManager.discardSession();
                window.handleSessionInvalid();
            }
            return Promise.reject(error);
        });
        return httpClient;
    }

    getResourceList(resource) {
        return this.getHTTPClient().get(resource);
    }

    getProxyServiceByName(name) {
        return this.getHTTPClient().get(`/proxy-services?proxyServiceName=${name}`);
    }

    getMessageStoreServiceByName(name) {
        return this.getHTTPClient().get(`/message-stores?name=${name}`);
    }

    getApiByName(name) {
        return this.getHTTPClient().get(`/apis?apiName=${name}`);
    }

    getMessageProcessorByName(name) {
        return this.getHTTPClient().get(`/message-processors?name=${name}`);
    }

    getLocalEntryByName(name) {
        return this.getHTTPClient().get(`/local-entries?name=${name}`);
    }

    getEndpointByName(name) {
        return this.getHTTPClient().get(`/endpoints?endpointName=${name}`);
    }

    getInboundEndpointByName(name) {
        return this.getHTTPClient().get(`/inbound-endpoints?inboundEndpointName=${name}`);
    }

    getSequenceByName(name) {
        return this.getHTTPClient().get(`/sequences?sequenceName=${name}`);
    }

    getTaskByName(name) {
        return this.getHTTPClient().get(`/tasks?taskName=${name}`);
    }

    getServerMetaData() {
        return this.getHTTPClient().get(`/server`);
    }
}
