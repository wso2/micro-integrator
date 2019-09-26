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


/**
 * HTTP status codes.
 */
const HttpStatus = {
    UNKNOWN: -1,
    OK: 200,
    CREATED: 201,
    CONFLICT: 409,
    FORBIDDEN: 403,
    NOTFOUND: 404,
    UNAUTHORIZED: 401,
    SERVE_ERROR: 500,
};

/**
 * Media types.
 */
const MediaType = {
    APPLICATION_WWW_FORM_URLENCODED: 'application/x-www-form-urlencoded',
    APPLICATION_JSON: 'application/json',
};

export { HttpStatus, MediaType };