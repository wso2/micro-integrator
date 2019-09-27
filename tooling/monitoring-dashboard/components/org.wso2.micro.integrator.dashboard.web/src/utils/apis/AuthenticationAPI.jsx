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
import {MediaType} from '../Constants';
import Qs from 'qs';

var baseURL = "";

export default class AuthenticationAPI {

    /**
     * Get HTTP client.
     *
     * @return {AxiosInstance} Axios client
     */
    static getHttpLogoutClient() {
        baseURL = `https://${window.localStorage.getItem('host')}:${window.localStorage.getItem('port')}/management`;
        const client = Axios.create({
            baseURL: baseURL,
            timeout: 300000,
        });
        client.defaults.headers.post['Content-Type'] = MediaType.APPLICATION_JSON;
        return client;
    }

    /**
     * Get HTTP client based on given host and port.
     *
     * @host host i.e: localhost
     * @port port of the MI i.e 9164
     * @return {AxiosInstance} Axios client
     */
    static getHttpLoginClient(host, port) {
        baseURL = `https://${host}:${port}/management`;
        const client = Axios.create({
            baseURL: baseURL,
            timeout: 300000,
        });
        client.defaults.headers.post['Content-Type'] = MediaType.APPLICATION_JSON;
        return client;
    }

    /**
     * Login user.
     *
     * @param {string} username Username
     * @param {string} password Password
     * @param {boolean} rememberMe Remember me flag
     * @param {string} grantType Grant type
     * @return {AxiosPromise} Axios promise
     */
    static login(host, port, username, password, rememberMe = false) {
        return AuthenticationAPI
            .getHttpLoginClient(host, port).get(`/login`, {
                auth: {
                    username: `${username}`,
                    password: `${password}`
                }
            });
    }


    /**
     * Logout user.
     *
     * @param {string} token Partial access token
     * @return {AxiosPromise} Axios promise
     */
    static logout(token) {
        return AuthenticationAPI
            .getHttpLogoutClient()
            .get(`/logout`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
    }
}


