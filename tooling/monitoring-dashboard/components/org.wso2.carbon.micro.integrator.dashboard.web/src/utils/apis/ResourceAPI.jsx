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

const baseURL = `https://localhost:9164/management`;
const https = require('https');

export default class ResourceAPI {

    getHTTPClient() {
        let httpClient = Axios.create({
            baseURL: baseURL,
            timeout: 30000,
            httpsAgent: new https.Agent({
                rejectUnauthorized: false
            })
        });
        httpClient.defaults.headers.post['Content-Type'] = 'application/json';
        httpClient.interceptors.response.use(function (response) {
            return response;
        }, function (error) {
            // handle error
        });
        return httpClient;
    }

    getResourceList(resource) {
        return this.getHTTPClient().get(resource);
    }

    getProxyServiceByName(name){
        return this.getHTTPClient().get(`/proxy-services?proxyServiceName=${name}`);
    }

    getMessageStoreServiceByName(name){
        return this.getHTTPClient().get(`/message-stores?name=${name}`);
    }

    getApiByName(name){
        return this.getHTTPClient().get(`/apis?apiName=${name}`);
    }

    getMessageProcessorByName(name){
        return this.getHTTPClient().get(`/message-processors?name=${name}`);
    }

}